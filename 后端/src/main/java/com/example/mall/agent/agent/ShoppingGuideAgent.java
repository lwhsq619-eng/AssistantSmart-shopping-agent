package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.RecommendedProductDTO;
import com.example.mall.agent.dto.agent.ShoppingGuideRequest;
import com.example.mall.agent.dto.agent.ShoppingGuideResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.agent.memory.ChatMemoryService;
import com.example.mall.agent.prompt.ShoppingGuidePrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.ProductTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 智能导购 Agent。
 * 商品数据只能来自 ProductTool，DeepSeek 只负责生成推荐话术。
 */
@Service
public class ShoppingGuideAgent {
    private static final Logger log = LoggerFactory.getLogger(ShoppingGuideAgent.class);
    private static final int RECOMMEND_LIMIT = 5;

    private final ProductTool productTool;
    private final DeepSeekService deepSeekService;
    private final ChatMemoryService chatMemoryService;
    private final ShoppingGuidePrompt shoppingGuidePrompt;

    public ShoppingGuideAgent(ProductTool productTool,
                              DeepSeekService deepSeekService,
                              ChatMemoryService chatMemoryService,
                              ShoppingGuidePrompt shoppingGuidePrompt) {
        this.productTool = productTool;
        this.deepSeekService = deepSeekService;
        this.chatMemoryService = chatMemoryService;
        this.shoppingGuidePrompt = shoppingGuidePrompt;
    }

    public ShoppingGuideResponse chat(ShoppingGuideRequest request) {
        String sessionId = chatMemoryService.normalizeSessionId(request == null ? null : request.getSessionId());
        if (request == null || request.getUserId() == null) {
            return ShoppingGuideResponse.fail(sessionId, "userId 不能为空");
        }
        if (!StringUtils.hasText(request.getMessage())) {
            return ShoppingGuideResponse.fail(sessionId, "message 不能为空");
        }

        long start = System.currentTimeMillis();
        String message = request.getMessage().trim();
        if (isCustomerServiceQuestion(message)) {
            String reply = "这个问题更适合智能客服处理，我可以帮你转到客服助手。";
            chatMemoryService.append(sessionId, message, reply);
            return ShoppingGuideResponse.builder()
                    .success(true)
                    .sessionId(sessionId)
                    .intent("客服问题")
                    .reply(reply)
                    .products(List.of())
                    .needMoreInfo(false)
                    .build();
        }

        String context = chatMemoryService.buildUserContext(sessionId, message);
        GuideQuery query = extractQuery(context);
        try {
            List<ProductToolResponse> productData = searchCandidateProducts(query);
            if (productData == null || productData.isEmpty()) {
                productData = productTool.getHotProducts(20).stream()
                        .filter(product -> query.maxPrice() == null
                                || product.getPrice() == null
                                || product.getPrice().compareTo(query.maxPrice()) <= 0)
                        .filter(product -> !StringUtils.hasText(query.categoryName())
                                || (product.getCategoryName() != null && product.getCategoryName().contains(query.categoryName())))
                        .toList();
            }
            List<RecommendedProductDTO> products = toRecommendedProducts(productData, query);
            String reply = buildReply(message, query, productData, products);
            chatMemoryService.append(sessionId, message, reply);
            log.info("ShoppingGuideAgent success, sessionId={}, userId={}, elapsedMs={}, count={}",
                    sessionId, request.getUserId(), System.currentTimeMillis() - start, products.size());
            return ShoppingGuideResponse.builder()
                    .success(true)
                    .sessionId(sessionId)
                    .intent("商品推荐")
                    .reply(reply)
                    .products(products)
                    .needMoreInfo(products.isEmpty())
                    .build();
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            log.error("ShoppingGuideAgent failed, sessionId={}, userId={}", sessionId, request.getUserId(), e);
            return ShoppingGuideResponse.fail(sessionId, "智能导购处理失败：" + e.getMessage());
        }
    }

    private List<ProductToolResponse> searchCandidateProducts(GuideQuery query) {
        Map<Long, ProductToolResponse> productMap = new LinkedHashMap<>();
        for (String keyword : expandKeywords(query)) {
            List<ProductToolResponse> products = productTool.searchProducts(keyword, query.categoryName(), null, query.maxPrice());
            for (ProductToolResponse product : products) {
                if (product.getProductId() != null) {
                    productMap.putIfAbsent(product.getProductId(), product);
                }
            }
            if (productMap.size() >= 20) {
                break;
            }
        }
        if (productMap.isEmpty() && StringUtils.hasText(query.categoryName())) {
            for (String keyword : expandKeywords(new GuideQuery(query.usageScene(), null, query.maxPrice(), query.usageScene()))) {
                List<ProductToolResponse> products = productTool.searchProducts(keyword, null, null, query.maxPrice());
                for (ProductToolResponse product : products) {
                    if (product.getProductId() != null) {
                        productMap.putIfAbsent(product.getProductId(), product);
                    }
                }
                if (productMap.size() >= 20) {
                    break;
                }
            }
        }
        return productMap.values().stream()
                .sorted(Comparator.comparing(ProductToolResponse::getSales, Comparator.nullsLast(Integer::compareTo)).reversed())
                .limit(20)
                .toList();
    }

    private List<String> expandKeywords(GuideQuery query) {
        List<String> keywords = new ArrayList<>();
        addKeyword(keywords, query.keyword());
        String scene = query.usageScene();
        if ("健身".equals(scene) || "运动".equals(scene) || "跑步".equals(scene)) {
            addKeyword(keywords, "运动裤");
            addKeyword(keywords, "运动鞋");
            addKeyword(keywords, "卫衣");
            addKeyword(keywords, "瑜伽垫");
            addKeyword(keywords, "跳绳");
            addKeyword(keywords, "泡沫轴");
        }
        if ("办公".equals(scene) || "学习".equals(scene)) {
            addKeyword(keywords, "键盘");
            addKeyword(keywords, "显示器");
            addKeyword(keywords, "台灯");
            addKeyword(keywords, "文件夹");
            addKeyword(keywords, "计算器");
        }
        if ("送礼".equals(scene)) {
            addKeyword(keywords, "礼品卡");
            addKeyword(keywords, "礼盒");
            addKeyword(keywords, "香水");
            addKeyword(keywords, "咖啡");
        }
        if (keywords.isEmpty()) {
            keywords.add(null);
        }
        return keywords;
    }

    private void addKeyword(List<String> keywords, String keyword) {
        if (StringUtils.hasText(keyword) && !keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }

    private String buildReply(String message,
                              GuideQuery query,
                              List<ProductToolResponse> productData,
                              List<RecommendedProductDTO> products) {
        if (products.isEmpty()) {
            return "没有找到符合条件的商品。建议放宽预算、减少场景限制，或换一个更宽泛的关键词。";
        }
        String fallbackReply = "AI服务暂时不可用，已根据商品数据为你生成基础推荐。\n" + buildRuleReply(products);
        try {
            String prompt = buildUserPrompt(message, query, productData);
            AiChatResponse aiResponse = deepSeekService.chat(shoppingGuidePrompt.systemPrompt(), prompt);
            if (Boolean.TRUE.equals(aiResponse.getSuccess()) && StringUtils.hasText(aiResponse.getContent())) {
                return aiResponse.getContent();
            }
            return fallbackReply;
        } catch (Exception e) {
            log.warn("ShoppingGuideAgent DeepSeek fallback, error={}", e.getMessage());
            return fallbackReply;
        }
    }

    private List<RecommendedProductDTO> toRecommendedProducts(List<ProductToolResponse> productData, GuideQuery query) {
        if (productData == null || productData.isEmpty()) {
            return List.of();
        }
        return productData.stream()
                .filter(product -> product.getStock() == null || product.getStock() > 0)
                .sorted(Comparator.comparing(this::hasRealImage).reversed()
                        .thenComparing(ProductToolResponse::getPrice, Comparator.nullsLast(BigDecimal::compareTo)))
                .limit(RECOMMEND_LIMIT)
                .map(product -> RecommendedProductDTO.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .imageUrl(product.getImageUrl())
                        .reason(buildReason(product, query))
                        .build())
                .toList();
    }

    private boolean hasRealImage(ProductToolResponse product) {
        return product != null
                && product.getImageUrl() != null
                && product.getImageUrl().startsWith("https://images.unsplash.com/");
    }

    private String buildReason(ProductToolResponse product, GuideQuery query) {
        List<String> reasons = new ArrayList<>();
        if (query.maxPrice() != null && product.getPrice() != null && product.getPrice().compareTo(query.maxPrice()) <= 0) {
            reasons.add("价格符合预算");
        }
        if (StringUtils.hasText(query.usageScene())) {
            reasons.add("适合" + query.usageScene() + "场景");
        }
        if (product.getStock() != null && product.getStock() > 0) {
            reasons.add("当前有库存");
        }
        if (StringUtils.hasText(product.getDiscountInfo()) && !product.getDiscountInfo().contains("暂无")) {
            reasons.add("有优惠信息");
        }
        return reasons.isEmpty() ? "根据商品名称、价格和库存匹配你的需求。" : String.join("，", reasons) + "。";
    }

    private String buildRuleReply(List<RecommendedProductDTO> products) {
        StringBuilder builder = new StringBuilder();
        for (RecommendedProductDTO product : products) {
            builder.append("- ")
                    .append(product.getProductName())
                    .append("，价格：").append(product.getPrice())
                    .append("，库存：").append(product.getStock())
                    .append("，理由：").append(product.getReason())
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildUserPrompt(String message, GuideQuery query, List<ProductToolResponse> products) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户需求：").append(message).append('\n');
        builder.append("抽取条件：keyword=").append(query.keyword())
                .append(", categoryName=").append(query.categoryName())
                .append(", maxPrice=").append(query.maxPrice())
                .append(", usageScene=").append(query.usageScene())
                .append('\n');
        builder.append("只能基于以下真实商品生成推荐说明，不得新增商品：\n");
        for (ProductToolResponse product : products) {
            builder.append("- 商品ID=").append(product.getProductId())
                    .append("，名称=").append(product.getProductName())
                    .append("，分类=").append(product.getCategoryName())
                    .append("，价格=").append(product.getPrice())
                    .append("，库存=").append(product.getStock())
                    .append("，销量=").append(product.getSales())
                    .append("，描述=").append(product.getDescription())
                    .append('\n');
        }
        builder.append("请用 120 字以内中文回答，说明推荐理由。");
        return builder.toString();
    }

    private boolean isCustomerServiceQuestion(String message) {
        return containsAny(message, "订单", "退款", "退货", "发货", "物流", "售后");
    }

    private GuideQuery extractQuery(String text) {
        return new GuideQuery(
                extractKeyword(text),
                extractCategoryName(text),
                extractMaxPrice(text),
                extractUsageScene(text)
        );
    }

    private String extractKeyword(String text) {
        if (containsAny(text, "健身穿", "运动穿", "跑步穿")) {
            return "运动裤";
        }
        if (containsAny(text, "衣服", "穿搭", "穿的", "服装") && containsAny(text, "健身", "运动", "跑步")) {
            return "运动裤";
        }
        if (containsAny(text, "衣服", "穿搭", "穿的", "服装")) {
            return "卫衣";
        }
        String[] candidates = {
                "无线耳机", "蓝牙耳机", "耳机", "机械键盘", "键盘", "显示器", "智能手表", "手机支架",
                "运动裤", "休闲裤", "运动鞋", "板鞋", "卫衣", "双肩包", "香水", "护肤套装", "咖啡", "零食礼盒", "保温杯",
                "台灯", "收纳盒", "瑜伽垫", "篮球", "图书", "文具", "母婴", "宠物玩具", "礼品卡"
        };
        for (String candidate : candidates) {
            if (text.contains(candidate)) {
                return candidate;
            }
        }
        return cleanupKeyword(text);
    }

    private String cleanupKeyword(String text) {
        String cleaned = text
                .replaceAll("\\d+(\\.\\d+)?\\s*元\\s*(以内|以下|之内|内)?", "")
                .replaceAll("推荐|帮我|我想买|想买|适合|最好|用|的", "")
                .trim();
        if (!StringUtils.hasText(cleaned) || cleaned.length() > 20) {
            return null;
        }
        return cleaned;
    }

    private String extractCategoryName(String text) {
        if (containsAny(text, "衣服", "穿搭", "穿的", "服装", "裤", "鞋", "卫衣")) {
            return "服饰";
        }
        if (containsAny(text, "健身器材", "壶铃", "泡沫轴", "健身踏板")) {
            return "健身";
        }
        String[] categories = {
                "数码科技", "手机通讯", "电脑办公", "家居生活", "服饰鞋包", "美妆护肤",
                "食品饮料", "运动户外", "图书文具", "办公文具", "箱包配饰", "图书音像",
                "母婴用品", "宠物用品", "礼品专区", "健身", "时尚服饰"
        };
        for (String category : categories) {
            if (text.contains(category)) {
                return category;
            }
        }
        return null;
    }

    private BigDecimal extractMaxPrice(String text) {
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*元?\\s*(以内|以下|之内|内)").matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        matcher = Pattern.compile("预算\\s*(\\d+(?:\\.\\d+)?)").matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private String extractUsageScene(String text) {
        String[] scenes = {"跑步", "运动", "防水", "办公", "学习", "通勤", "送礼", "旅行", "居家", "健身", "游戏"};
        for (String scene : scenes) {
            if (text.contains(scene)) {
                return scene;
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private record GuideQuery(String keyword, String categoryName, BigDecimal maxPrice, String usageScene) {
    }
}
