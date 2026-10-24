package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.SceneMatchGroup;
import com.example.mall.agent.dto.agent.SceneMatchRequest;
import com.example.mall.agent.dto.agent.SceneMatchResponse;
import com.example.mall.agent.dto.agent.SceneProductItem;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.SceneMatchPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.tool.UserTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 场景化搭配推荐 Agent。
 * Java 负责真实商品筛选和预算内组合，DeepSeek 只生成搭配名称与理由。
 */
@Service
public class SceneMatchAgent {
    private static final Logger log = LoggerFactory.getLogger(SceneMatchAgent.class);
    private static final Set<String> SCENE_TYPES = Set.of("OUTFIT", "HOME", "DIGITAL", "DORMITORY", "GIFT", "OFFICE");
    private static final Map<String, List<String>> SCENE_KEYWORDS = Map.of(
            "DORMITORY", List.of("台灯", "收纳", "插排", "清洁", "床上用品"),
            "DIGITAL", List.of("手机壳", "充电器", "数据线", "耳机", "支架"),
            "OUTFIT", List.of("上衣", "裤子", "鞋子", "包", "配饰"),
            "HOME", List.of("灯具", "抱枕", "收纳", "香薰", "装饰"),
            "GIFT", List.of("礼盒", "香薰", "数码", "饰品", "零食"),
            "OFFICE", List.of("键盘", "鼠标", "支架", "台灯", "收纳")
    );
    private static final Map<String, List<String>> SCENE_CATEGORY_HINTS = Map.of(
            "DORMITORY", List.of("家居生活", "图书文具", "电脑办公"),
            "DIGITAL", List.of("数码科技", "手机通讯", "电脑办公"),
            "OUTFIT", List.of("服饰鞋包", "运动户外"),
            "HOME", List.of("家居生活"),
            "GIFT", List.of("礼品专区", "食品饮料", "美妆护肤"),
            "OFFICE", List.of("电脑办公", "图书文具", "家居生活")
    );

    private final ProductTool productTool;
    private final UserTool userTool;
    private final AiService aiService;
    private final SceneMatchPrompt sceneMatchPrompt;
    private final ObjectMapper objectMapper;

    public SceneMatchAgent(ProductTool productTool,
                           UserTool userTool,
                           AiService aiService,
                           SceneMatchPrompt sceneMatchPrompt,
                           ObjectMapper objectMapper) {
        this.productTool = productTool;
        this.userTool = userTool;
        this.aiService = aiService;
        this.sceneMatchPrompt = sceneMatchPrompt;
        this.objectMapper = objectMapper;
    }

    public SceneMatchResponse match(SceneMatchRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return SceneMatchResponse.fail(
                    request == null ? null : request.getSceneType(),
                    request == null ? null : request.getBudget(),
                    validationError);
        }

        String sceneType = request.getSceneType().trim().toUpperCase(Locale.ROOT);
        int limit = normalizeLimit(request.getLimit());
        long start = System.currentTimeMillis();
        try {
            userTool.getUserProfile(request.getUserId());
            List<String> keywords = buildKeywords(sceneType, request.getMessage());
            List<ProductToolResponse> candidates = collectCandidates(sceneType, keywords, request.getBudget());
            List<SceneProductItem> selected = buildBudgetGroup(candidates, keywords, request.getBudget(), limit);
            if (selected.isEmpty()) {
                return SceneMatchResponse.fail(sceneType, request.getBudget(), "预算内暂无可搭配商品。");
            }

            BigDecimal totalPrice = sumPrice(selected);
            SceneMatchGroup baseGroup = SceneMatchGroup.builder()
                    .groupName("基础场景搭配组合")
                    .reason("根据预算、库存和品类匹配生成。")
                    .totalPrice(totalPrice)
                    .products(selected)
                    .build();

            AiGroupResult aiGroup = generateGroupCopy(request, sceneType, keywords, baseGroup);
            SceneMatchGroup finalGroup = applyAiGroup(baseGroup, aiGroup);
            log.info("SceneMatchAgent success, userId={}, sceneType={}, elapsedMs={}, productCount={}",
                    request.getUserId(), sceneType, System.currentTimeMillis() - start, finalGroup.getProducts().size());
            return SceneMatchResponse.builder()
                    .success(true)
                    .sceneType(sceneType)
                    .budget(request.getBudget())
                    .totalPrice(finalGroup.getTotalPrice())
                    .groups(List.of(finalGroup))
                    .fallbackUsed(aiGroup.fallbackUsed())
                    .message("搭配推荐生成成功")
                    .build();
        } catch (Exception e) {
            log.error("SceneMatchAgent failed, userId={}, sceneType={}", request.getUserId(), sceneType, e);
            return SceneMatchResponse.fail(sceneType, request.getBudget(), "场景搭配推荐失败：" + e.getMessage());
        }
    }

    private List<ProductToolResponse> collectCandidates(String sceneType, List<String> keywords, BigDecimal budget) {
        Map<Long, ProductToolResponse> candidates = new LinkedHashMap<>();
        for (String keyword : keywords) {
            addCandidates(candidates, productTool.searchProducts(keyword, null, null, budget));
        }
        for (String category : SCENE_CATEGORY_HINTS.getOrDefault(sceneType, List.of())) {
            addCandidates(candidates, productTool.searchProducts(null, category, null, budget));
        }
        if (candidates.size() < 10) {
            addCandidates(candidates, productTool.getDiscountProducts(30));
            addCandidates(candidates, productTool.getHotProducts(30));
        }
        return candidates.values().stream()
                .filter(product -> safeStock(product) > 0)
                .filter(product -> product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .filter(product -> product.getPrice().compareTo(budget) <= 0)
                .toList();
    }

    private List<SceneProductItem> buildBudgetGroup(List<ProductToolResponse> candidates,
                                                    List<String> keywords,
                                                    BigDecimal budget,
                                                    int limit) {
        List<ProductToolResponse> sortedCandidates = candidates.stream()
                .sorted(Comparator.comparingInt((ProductToolResponse product) -> matchScore(product, keywords)).reversed()
                        .thenComparing(ProductToolResponse::getPrice, Comparator.nullsLast(BigDecimal::compareTo))
                        .thenComparing(ProductToolResponse::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<SceneProductItem> selected = new ArrayList<>();
        Set<Long> selectedIds = new LinkedHashSet<>();
        Set<String> selectedCategories = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (String keyword : keywords) {
            if (selected.size() >= limit) {
                break;
            }
            ProductToolResponse product = bestProductForKeyword(sortedCandidates, keyword, budget.subtract(total), selectedIds);
            if (product != null) {
                selected.add(toSceneProductItem(product, "符合当前场景需求。"));
                selectedIds.add(product.getProductId());
                if (StringUtils.hasText(product.getCategoryName())) {
                    selectedCategories.add(product.getCategoryName());
                }
                total = total.add(product.getPrice());
            }
        }

        int targetMin = Math.min(3, limit);
        for (ProductToolResponse product : sortedCandidates) {
            if (selected.size() >= limit || (selected.size() >= targetMin && total.compareTo(budget.multiply(BigDecimal.valueOf(0.85))) >= 0)) {
                break;
            }
            if (selectedIds.contains(product.getProductId()) || total.add(product.getPrice()).compareTo(budget) > 0) {
                continue;
            }
            boolean categoryPreferred = !selectedCategories.contains(product.getCategoryName()) || selected.size() < targetMin;
            if (categoryPreferred) {
                selected.add(toSceneProductItem(product, "符合当前场景需求。"));
                selectedIds.add(product.getProductId());
                if (StringUtils.hasText(product.getCategoryName())) {
                    selectedCategories.add(product.getCategoryName());
                }
                total = total.add(product.getPrice());
            }
        }

        if (selected.isEmpty()) {
            return cheapestUnderBudget(sortedCandidates, budget, limit).stream()
                    .map(product -> toSceneProductItem(product, "符合当前场景需求。"))
                    .toList();
        }
        return selected;
    }

    private ProductToolResponse bestProductForKeyword(List<ProductToolResponse> candidates,
                                                      String keyword,
                                                      BigDecimal remainingBudget,
                                                      Set<Long> selectedIds) {
        return candidates.stream()
                .filter(product -> !selectedIds.contains(product.getProductId()))
                .filter(product -> product.getPrice().compareTo(remainingBudget) <= 0)
                .filter(product -> productText(product).contains(keyword.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
    }

    private List<ProductToolResponse> cheapestUnderBudget(List<ProductToolResponse> candidates, BigDecimal budget, int limit) {
        List<ProductToolResponse> selected = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ProductToolResponse product : candidates.stream()
                .sorted(Comparator.comparing(ProductToolResponse::getPrice, Comparator.nullsLast(BigDecimal::compareTo)))
                .toList()) {
            if (selected.size() >= limit) {
                break;
            }
            if (total.add(product.getPrice()).compareTo(budget) <= 0) {
                selected.add(product);
                total = total.add(product.getPrice());
            }
        }
        return selected;
    }

    private AiGroupResult generateGroupCopy(SceneMatchRequest request,
                                            String sceneType,
                                            List<String> keywords,
                                            SceneMatchGroup group) {
        try {
            String prompt = buildPrompt(request, sceneType, keywords, group);
            AiChatResponse response = isComplexScene(sceneType, request.getMessage(), group.getProducts().size())
                    ? aiService.reason(sceneMatchPrompt.systemPrompt(), prompt)
                    : aiService.chat(sceneMatchPrompt.systemPrompt(), prompt);
            if (!Boolean.TRUE.equals(response.getSuccess()) || !StringUtils.hasText(response.getContent())) {
                return fallbackGroupResult(group);
            }
            return parseAiGroup(response.getContent(), group);
        } catch (Exception e) {
            log.warn("SceneMatchAgent AI fallback, sceneType={}, error={}", sceneType, e.getMessage());
            return fallbackGroupResult(group);
        }
    }

    private String buildPrompt(SceneMatchRequest request,
                               String sceneType,
                               List<String> keywords,
                               SceneMatchGroup group) {
        return "请基于系统已生成的真实商品组合，补充组合名称、整体搭配理由和每个商品的 matchReason，只返回 JSON。\n"
                + "场景类型：" + sceneType + "\n"
                + "用户需求：" + request.getMessage() + "\n"
                + "预算：" + request.getBudget() + "\n"
                + "场景关键词：" + keywords + "\n"
                + "Java 已生成组合：" + toJson(group) + "\n"
                + "禁止新增商品，禁止修改价格、库存、销量。\n"
                + "JSON 格式：{\"groups\":[{\"groupName\":\"\",\"reason\":\"\",\"items\":[{\"productId\":1001,\"matchReason\":\"\"}]}]}";
    }

    private AiGroupResult parseAiGroup(String content, SceneMatchGroup baseGroup) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(content));
        JsonNode groups = root.get("groups");
        if (groups == null || !groups.isArray() || groups.isEmpty()) {
            return fallbackGroupResult(baseGroup);
        }
        JsonNode group = groups.get(0);
        String groupName = textOrDefault(group, "groupName", "基础场景搭配组合");
        String reason = textOrDefault(group, "reason", "根据预算、库存和品类匹配生成。");
        Map<Long, String> itemReasons = new LinkedHashMap<>();
        JsonNode items = group.get("items");
        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                if (item.path("productId").isNumber() && StringUtils.hasText(item.path("matchReason").asText())) {
                    itemReasons.put(item.path("productId").asLong(), item.path("matchReason").asText());
                }
            }
        }
        return new AiGroupResult(groupName, reason, itemReasons, false);
    }

    private AiGroupResult fallbackGroupResult(SceneMatchGroup group) {
        Map<Long, String> reasons = new LinkedHashMap<>();
        for (SceneProductItem product : group.getProducts()) {
            reasons.put(product.getProductId(), "符合当前场景需求。");
        }
        return new AiGroupResult("基础场景搭配组合", "根据预算、库存和品类匹配生成。", reasons, true);
    }

    private SceneMatchGroup applyAiGroup(SceneMatchGroup baseGroup, AiGroupResult result) {
        for (SceneProductItem product : baseGroup.getProducts()) {
            product.setMatchReason(result.itemReasons().getOrDefault(product.getProductId(), "符合当前场景需求。"));
        }
        return SceneMatchGroup.builder()
                .groupName(result.groupName())
                .reason(result.reason())
                .totalPrice(baseGroup.getTotalPrice())
                .products(baseGroup.getProducts())
                .build();
    }

    private List<String> buildKeywords(String sceneType, String message) {
        Set<String> keywords = new LinkedHashSet<>(SCENE_KEYWORDS.getOrDefault(sceneType, List.of()));
        String text = message == null ? "" : message;
        addIfContains(text, keywords, "便宜", "低价");
        addIfContains(text, keywords, "实用", "实用");
        addIfContains(text, keywords, "生日", "礼盒");
        addIfContains(text, keywords, "学习", "台灯");
        addIfContains(text, keywords, "手机", "手机");
        addIfContains(text, keywords, "电脑", "电脑");
        addIfContains(text, keywords, "运动", "运动");
        return new ArrayList<>(keywords);
    }

    private void addIfContains(String text, Set<String> keywords, String trigger, String keyword) {
        if (StringUtils.hasText(text) && text.contains(trigger)) {
            keywords.add(keyword);
        }
    }

    private void addCandidates(Map<Long, ProductToolResponse> candidates, List<ProductToolResponse> products) {
        for (ProductToolResponse product : products == null ? List.<ProductToolResponse>of() : products) {
            if (product.getProductId() != null) {
                candidates.putIfAbsent(product.getProductId(), product);
            }
        }
    }

    private int matchScore(ProductToolResponse product, List<String> keywords) {
        String text = productText(product);
        int score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                score += 20;
            }
        }
        score += Math.min(product.getSales() == null ? 0 : product.getSales(), 200) / 20;
        if (StringUtils.hasText(product.getDiscountInfo()) && !"暂无折扣".equals(product.getDiscountInfo())) {
            score += 5;
        }
        return score;
    }

    private String productText(ProductToolResponse product) {
        return (nullToEmpty(product.getProductName()) + " "
                + nullToEmpty(product.getCategoryName()) + " "
                + nullToEmpty(product.getDescription())).toLowerCase(Locale.ROOT);
    }

    private SceneProductItem toSceneProductItem(ProductToolResponse product, String reason) {
        return SceneProductItem.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .categoryName(product.getCategoryName())
                .price(product.getPrice())
                .stock(product.getStock())
                .sales(product.getSales())
                .imageUrl(product.getImageUrl())
                .discountInfo(product.getDiscountInfo())
                .matchReason(reason)
                .build();
    }

    private BigDecimal sumPrice(List<SceneProductItem> products) {
        return products.stream()
                .map(SceneProductItem::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isComplexScene(String sceneType, String message, int productCount) {
        return productCount >= 5
                || "OUTFIT".equals(sceneType)
                || "HOME".equals(sceneType)
                || (StringUtils.hasText(message) && (message.contains("风格") || message.contains("成套") || message.contains("礼物")));
    }

    private String validateRequest(SceneMatchRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        if (request.getUserId() == null) {
            return "userId 不能为空";
        }
        if (!StringUtils.hasText(request.getSceneType())) {
            return "sceneType 不能为空";
        }
        if (!SCENE_TYPES.contains(request.getSceneType().trim().toUpperCase(Locale.ROOT))) {
            return "sceneType 非法";
        }
        if (request.getBudget() == null || request.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return "budget 必须大于 0";
        }
        if (!StringUtils.hasText(request.getMessage())) {
            return "message 不能为空";
        }
        if (request.getLimit() != null && request.getLimit() < 1) {
            return "limit 不能小于 1";
        }
        if (request.getLimit() != null && request.getLimit() > 10) {
            return "limit 不能超过 10";
        }
        return null;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 5;
        }
        return Math.min(Math.max(limit, 1), 10);
    }

    private int safeStock(ProductToolResponse product) {
        return product.getStock() == null ? 0 : product.getStock();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return String.valueOf(object);
        }
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value != null && value.isTextual() && StringUtils.hasText(value.asText())) {
            return value.asText();
        }
        return defaultValue;
    }

    private record AiGroupResult(String groupName,
                                 String reason,
                                 Map<Long, String> itemReasons,
                                 boolean fallbackUsed) {
    }
}
