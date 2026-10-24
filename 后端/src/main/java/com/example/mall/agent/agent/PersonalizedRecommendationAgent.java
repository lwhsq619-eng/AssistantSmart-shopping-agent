package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.PersonalizedRecommendationRequest;
import com.example.mall.agent.dto.agent.PersonalizedRecommendationResponse;
import com.example.mall.agent.dto.agent.RecommendationProductItem;
import com.example.mall.agent.dto.agent.UserPreferenceProfile;
import com.example.mall.agent.dto.tool.OrderItemToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.prompt.PersonalizedRecommendationPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.OrderTool;
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
 * 千人千面个性化商品推荐 Agent。
 * 推荐排序在 Java 侧完成，DeepSeek 只生成推荐解释，不改变商品集合和排序。
 */
@Service
public class PersonalizedRecommendationAgent {
    private static final Logger log = LoggerFactory.getLogger(PersonalizedRecommendationAgent.class);
    private static final Set<String> SCENES = Set.of(
            "HOME_PAGE",
            "PRODUCT_DETAIL",
            "CART_PAGE",
            "ORDER_AFTER_PAY",
            "SEARCH_RESULT",
            "ACTIVITY_PAGE"
    );
    private static final Set<String> REPURCHASE_CATEGORIES = Set.of("食品饮料", "美妆护肤", "母婴用品", "宠物用品");

    private final UserTool userTool;
    private final ProductTool productTool;
    private final OrderTool orderTool;
    private final AiService aiService;
    private final PersonalizedRecommendationPrompt recommendationPrompt;
    private final ObjectMapper objectMapper;

    public PersonalizedRecommendationAgent(UserTool userTool,
                                           ProductTool productTool,
                                           OrderTool orderTool,
                                           AiService aiService,
                                           PersonalizedRecommendationPrompt recommendationPrompt,
                                           ObjectMapper objectMapper) {
        this.userTool = userTool;
        this.productTool = productTool;
        this.orderTool = orderTool;
        this.aiService = aiService;
        this.recommendationPrompt = recommendationPrompt;
        this.objectMapper = objectMapper;
    }

    public PersonalizedRecommendationResponse recommend(PersonalizedRecommendationRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return PersonalizedRecommendationResponse.fail(
                    request == null ? null : request.getUserId(),
                    request == null ? null : request.getScene(),
                    validationError);
        }

        String scene = request.getScene().trim().toUpperCase(Locale.ROOT);
        int limit = normalizeLimit(request.getLimit());
        long start = System.currentTimeMillis();
        try {
            UserToolResponse profile = userTool.getUserProfile(request.getUserId());
            UserToolResponse behavior = userTool.getUserBehaviorSummary(request.getUserId());
            List<String> preferenceTags = safeList(userTool.getUserPreference(request.getUserId()));
            List<String> coupons = safeList(userTool.getUserCouponList(request.getUserId()));
            List<OrderToolResponse> orders = safeList(orderTool.getUserOrders(request.getUserId(), 20));

            UserPreferenceProfile preferenceProfile = buildPreferenceProfile(profile, behavior, preferenceTags, coupons, orders);
            List<ProductToolResponse> candidates = collectCandidates(preferenceProfile);
            List<RecommendationProductItem> rankedProducts = rankProducts(candidates, preferenceProfile, scene, limit);

            if (rankedProducts.isEmpty()) {
                rankedProducts = productTool.getHotProducts(limit).stream()
                        .filter(product -> safeStock(product) > 0)
                        .map(product -> toRecommendationItem(product, 40.0, fallbackReason(product, preferenceProfile)))
                        .toList();
            }

            AiReasonResult reasonResult = generateReasons(preferenceProfile, scene, rankedProducts);
            List<RecommendationProductItem> productsWithReasons = applyReasons(rankedProducts, reasonResult.reasons(), preferenceProfile);

            log.info("PersonalizedRecommendationAgent success, userId={}, scene={}, elapsedMs={}, count={}",
                    request.getUserId(), scene, System.currentTimeMillis() - start, productsWithReasons.size());
            return PersonalizedRecommendationResponse.builder()
                    .success(true)
                    .userId(request.getUserId())
                    .scene(scene)
                    .groupTitle(reasonResult.groupTitle())
                    .preferenceSummary(reasonResult.preferenceSummary())
                    .preferenceTags(preferenceProfile.getPreferenceTags())
                    .products(productsWithReasons)
                    .fallbackUsed(reasonResult.fallbackUsed())
                    .message(productsWithReasons.isEmpty() ? "暂无可推荐商品。" : "推荐成功")
                    .build();
        } catch (Exception e) {
            log.error("PersonalizedRecommendationAgent failed, userId={}, scene={}", request.getUserId(), scene, e);
            return PersonalizedRecommendationResponse.fail(request.getUserId(), scene, "个性化推荐失败：" + e.getMessage());
        }
    }

    private UserPreferenceProfile buildPreferenceProfile(UserToolResponse profile,
                                                         UserToolResponse behavior,
                                                         List<String> preferenceTags,
                                                         List<String> coupons,
                                                         List<OrderToolResponse> orders) {
        Set<String> tagSet = new LinkedHashSet<>();
        tagSet.addAll(safeList(behavior.getHistoryCategories()));
        tagSet.addAll(preferenceTags);

        Set<Long> purchasedProductIds = new LinkedHashSet<>();
        List<BigDecimal> prices = new ArrayList<>();
        for (OrderToolResponse order : orders) {
            for (OrderItemToolResponse item : safeList(order.getItems())) {
                if (item.getProductId() != null) {
                    purchasedProductIds.add(item.getProductId());
                    ProductToolResponse product = safeProductDetail(item.getProductId());
                    if (product != null && StringUtils.hasText(product.getCategoryName())) {
                        tagSet.add(product.getCategoryName());
                    }
                    if (item.getUnitPrice() != null) {
                        prices.add(item.getUnitPrice());
                    } else if (product != null && product.getPrice() != null) {
                        prices.add(product.getPrice());
                    }
                }
            }
        }

        BigDecimal averagePrice = average(prices);
        boolean dataInsufficient = tagSet.isEmpty() && purchasedProductIds.isEmpty();
        return UserPreferenceProfile.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .level(profile.getLevel())
                .historyCategories(new ArrayList<>(safeList(behavior.getHistoryCategories())))
                .preferenceTags(new ArrayList<>(tagSet))
                .coupons(coupons)
                .purchasedProductIds(purchasedProductIds)
                .averagePurchasePrice(averagePrice)
                .behaviorDataInsufficient(dataInsufficient)
                .build();
    }

    private List<ProductToolResponse> collectCandidates(UserPreferenceProfile profile) {
        Map<Long, ProductToolResponse> candidates = new LinkedHashMap<>();
        for (String tag : safeList(profile.getPreferenceTags())) {
            addCandidates(candidates, productTool.searchProducts(tag, null, null, null));
            addCandidates(candidates, productTool.searchProducts(null, tag, null, null));
            if (candidates.size() >= 50) {
                break;
            }
        }
        addCandidates(candidates, productTool.getDiscountProducts(30));
        addCandidates(candidates, productTool.getHotProducts(50));
        return candidates.values().stream().limit(50).toList();
    }

    private void addCandidates(Map<Long, ProductToolResponse> candidates, List<ProductToolResponse> products) {
        for (ProductToolResponse product : safeList(products)) {
            if (product.getProductId() != null) {
                candidates.putIfAbsent(product.getProductId(), product);
            }
        }
    }

    private List<RecommendationProductItem> rankProducts(List<ProductToolResponse> candidates,
                                                         UserPreferenceProfile profile,
                                                         String scene,
                                                         int limit) {
        return candidates.stream()
                .filter(product -> safeStock(product) > 0)
                .map(product -> toRecommendationItem(product, scoreProduct(product, profile, scene), null))
                .filter(item -> item.getScore() != null && item.getScore() >= 40.0)
                .sorted(Comparator.comparing(RecommendationProductItem::getScore, Comparator.reverseOrder())
                        .thenComparing(RecommendationProductItem::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(item -> {
                    item.setReason(fallbackReason(item, profile));
                    return item;
                })
                .toList();
    }

    private double scoreProduct(ProductToolResponse product, UserPreferenceProfile profile, String scene) {
        double score = 0;
        score += categoryScore(product, profile);
        score += priceScore(product, profile);
        score += tagScore(product, profile);
        score += hotScore(product);
        score += discountScore(product, scene);
        score += safeStock(product) > 0 ? 5 : 0;

        if (profile.getPurchasedProductIds().contains(product.getProductId())
                && !REPURCHASE_CATEGORIES.contains(product.getCategoryName())) {
            score -= 10;
        }
        return Math.max(0, Math.min(100, BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP).doubleValue()));
    }

    private double categoryScore(ProductToolResponse product, UserPreferenceProfile profile) {
        if (!StringUtils.hasText(product.getCategoryName())) {
            return 0;
        }
        for (String category : safeList(profile.getHistoryCategories())) {
            if (product.getCategoryName().contains(category) || category.contains(product.getCategoryName())) {
                return 30;
            }
        }
        for (String tag : safeList(profile.getPreferenceTags())) {
            if (product.getCategoryName().contains(tag) || tag.contains(product.getCategoryName())) {
                return 20;
            }
        }
        return Boolean.TRUE.equals(profile.getBehaviorDataInsufficient()) ? 10 : 0;
    }

    private double priceScore(ProductToolResponse product, UserPreferenceProfile profile) {
        if (product.getPrice() == null) {
            return 0;
        }
        if (profile.getAveragePurchasePrice() == null || profile.getAveragePurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return 10;
        }
        BigDecimal min = profile.getAveragePurchasePrice().multiply(BigDecimal.valueOf(0.6));
        BigDecimal max = profile.getAveragePurchasePrice().multiply(BigDecimal.valueOf(1.4));
        if (product.getPrice().compareTo(min) >= 0 && product.getPrice().compareTo(max) <= 0) {
            return 20;
        }
        if (product.getPrice().compareTo(profile.getAveragePurchasePrice().multiply(BigDecimal.valueOf(2))) <= 0) {
            return 10;
        }
        return 0;
    }

    private double tagScore(ProductToolResponse product, UserPreferenceProfile profile) {
        String text = (nullToEmpty(product.getProductName()) + " "
                + nullToEmpty(product.getCategoryName()) + " "
                + nullToEmpty(product.getDescription())).toLowerCase(Locale.ROOT);
        int matches = 0;
        for (String tag : safeList(profile.getPreferenceTags())) {
            if (StringUtils.hasText(tag) && text.contains(tag.toLowerCase(Locale.ROOT))) {
                matches++;
            }
        }
        if (matches >= 2) {
            return 20;
        }
        if (matches == 1) {
            return 12;
        }
        return Boolean.TRUE.equals(profile.getBehaviorDataInsufficient()) ? 8 : 0;
    }

    private double hotScore(ProductToolResponse product) {
        int sales = product.getSales() == null ? 0 : product.getSales();
        if (sales >= 200) {
            return 15;
        }
        if (sales >= 100) {
            return 12;
        }
        if (sales >= 50) {
            return 9;
        }
        if (sales > 0) {
            return 5;
        }
        return 0;
    }

    private double discountScore(ProductToolResponse product, String scene) {
        boolean hasDiscount = StringUtils.hasText(product.getDiscountInfo()) && !"暂无折扣".equals(product.getDiscountInfo());
        if (!hasDiscount) {
            return 0;
        }
        return "ACTIVITY_PAGE".equals(scene) || "CART_PAGE".equals(scene) ? 10 : 8;
    }

    private RecommendationProductItem toRecommendationItem(ProductToolResponse product, double score, String reason) {
        return RecommendationProductItem.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .categoryName(product.getCategoryName())
                .price(product.getPrice())
                .stock(product.getStock())
                .sales(product.getSales())
                .imageUrl(product.getImageUrl())
                .discountInfo(product.getDiscountInfo())
                .score(score)
                .reason(reason)
                .build();
    }

    private AiReasonResult generateReasons(UserPreferenceProfile profile,
                                           String scene,
                                           List<RecommendationProductItem> products) {
        if (products.isEmpty()) {
            return new AiReasonResult("暂无推荐", "暂无可推荐商品。", Map.of(), true);
        }
        try {
            AiChatResponse aiResponse = aiService.chat(recommendationPrompt.systemPrompt(), buildReasonPrompt(profile, scene, products));
            if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
                return fallbackReasonResult(profile, products);
            }
            return parseAiReason(aiResponse.getContent(), profile, products);
        } catch (Exception e) {
            log.warn("PersonalizedRecommendationAgent reason fallback, userId={}, error={}", profile.getUserId(), e.getMessage());
            return fallbackReasonResult(profile, products);
        }
    }

    private String buildReasonPrompt(UserPreferenceProfile profile, String scene, List<RecommendationProductItem> products) {
        return "请基于给定用户画像和商品列表，为每个商品生成推荐理由，只返回 JSON。\n"
                + "场景：" + scene + "\n"
                + "用户画像：" + toJson(profile) + "\n"
                + "Java 已排序商品列表：" + toJson(products) + "\n"
                + "要求：不能新增商品，不能改变价格、库存、销量，items 只返回 productId 和 reason。\n"
                + "JSON 格式：{\"groupTitle\":\"\",\"preferenceSummary\":\"\",\"items\":[{\"productId\":1001,\"reason\":\"\"}]}";
    }

    private AiReasonResult parseAiReason(String content,
                                         UserPreferenceProfile profile,
                                         List<RecommendationProductItem> products) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(content));
        Map<Long, String> reasons = new LinkedHashMap<>();
        JsonNode items = root.get("items");
        if (items != null && items.isArray()) {
            for (JsonNode item : items) {
                Long productId = item.path("productId").isNumber() ? item.path("productId").asLong() : null;
                String reason = item.path("reason").asText(null);
                if (productId != null && StringUtils.hasText(reason)) {
                    reasons.put(productId, reason);
                }
            }
        }
        String groupTitle = textOrDefault(root, "groupTitle", "为你精选");
        String preferenceSummary = textOrDefault(root, "preferenceSummary", buildPreferenceSummary(profile));
        if (reasons.isEmpty()) {
            return fallbackReasonResult(profile, products);
        }
        return new AiReasonResult(groupTitle, preferenceSummary, reasons, false);
    }

    private AiReasonResult fallbackReasonResult(UserPreferenceProfile profile, List<RecommendationProductItem> products) {
        Map<Long, String> reasons = new LinkedHashMap<>();
        for (RecommendationProductItem product : products) {
            reasons.put(product.getProductId(), fallbackReason(product, profile));
        }
        return new AiReasonResult("为你精选", buildPreferenceSummary(profile), reasons, true);
    }

    private List<RecommendationProductItem> applyReasons(List<RecommendationProductItem> products,
                                                         Map<Long, String> reasons,
                                                         UserPreferenceProfile profile) {
        for (RecommendationProductItem product : products) {
            String reason = reasons.get(product.getProductId());
            product.setReason(StringUtils.hasText(reason) ? reason : fallbackReason(product, profile));
        }
        return products;
    }

    private String fallbackReason(ProductToolResponse product, UserPreferenceProfile profile) {
        return fallbackReason(toRecommendationItem(product, 0, null), profile);
    }

    private String fallbackReason(RecommendationProductItem product, UserPreferenceProfile profile) {
        if (Boolean.TRUE.equals(profile.getBehaviorDataInsufficient())) {
            return "基于热销和相似商品推荐。";
        }
        if (profile.getHistoryCategories().contains(product.getCategoryName())) {
            return "与你近期关注的品类相似。";
        }
        if (product.getPrice() != null && profile.getAveragePurchasePrice() != null
                && profile.getAveragePurchasePrice().compareTo(BigDecimal.ZERO) > 0) {
            return "价格接近你的历史消费区间。";
        }
        if (product.getSales() != null && product.getSales() > 0) {
            return "该商品近期销量较高。";
        }
        if (StringUtils.hasText(product.getDiscountInfo()) && !"暂无折扣".equals(product.getDiscountInfo())) {
            return "该商品当前有可用优惠信息。";
        }
        return "基于你的偏好标签为你推荐。";
    }

    private String buildPreferenceSummary(UserPreferenceProfile profile) {
        if (Boolean.TRUE.equals(profile.getBehaviorDataInsufficient())) {
            return "用户行为数据不足，本次主要基于热销和相似商品推荐。";
        }
        return "根据历史订单、购物偏好和近期可用商品生成推荐。";
    }

    private ProductToolResponse safeProductDetail(Long productId) {
        try {
            return productTool.getProductDetail(productId);
        } catch (Exception e) {
            log.warn("PersonalizedRecommendationAgent product detail skipped, productId={}, error={}", productId, e.getMessage());
            return null;
        }
    }

    private String validateRequest(PersonalizedRecommendationRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        if (request.getUserId() == null) {
            return "userId 不能为空";
        }
        if (!StringUtils.hasText(request.getScene())) {
            return "scene 不能为空";
        }
        if (!SCENES.contains(request.getScene().trim().toUpperCase(Locale.ROOT))) {
            return "scene 非法";
        }
        if (request.getLimit() != null && request.getLimit() < 1) {
            return "limit 不能小于 1";
        }
        if (request.getLimit() != null && request.getLimit() > 20) {
            return "limit 不能超过 20";
        }
        return null;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 10;
        }
        return Math.min(Math.max(limit, 1), 20);
    }

    private int safeStock(ProductToolResponse product) {
        return product.getStock() == null ? 0 : product.getStock();
    }

    private BigDecimal average(List<BigDecimal> prices) {
        List<BigDecimal> validPrices = prices.stream()
                .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (validPrices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = validPrices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(validPrices.size()), 2, RoundingMode.HALF_UP);
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
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

    private record AiReasonResult(String groupTitle,
                                  String preferenceSummary,
                                  Map<Long, String> reasons,
                                  boolean fallbackUsed) {
    }
}
