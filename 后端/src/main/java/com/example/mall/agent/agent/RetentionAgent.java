package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.RetentionProductResult;
import com.example.mall.agent.dto.agent.RetentionRequest;
import com.example.mall.agent.dto.agent.RetentionResponse;
import com.example.mall.agent.dto.agent.RetentionStrategyResult;
import com.example.mall.agent.dto.tool.OrderItemToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.agent.memory.ChatMemoryService;
import com.example.mall.agent.prompt.RetentionPrompt;
import com.example.mall.agent.service.DeepSeekService;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 流失用户 AI 挽留 Agent。
 * 只生成挽留策略和话术，不发券、不改价、不创建订单、不发送站外消息。
 */
@Service
public class RetentionAgent {
    private static final Logger log = LoggerFactory.getLogger(RetentionAgent.class);
    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(10);

    private static final String CART_ABANDON = "CART_ABANDON";
    private static final String PAGE_EXIT = "PAGE_EXIT";
    private static final String LONG_STAY_NO_ORDER = "LONG_STAY_NO_ORDER";
    private static final String SEARCH_NO_CLICK = "SEARCH_NO_CLICK";
    private static final String PRODUCT_COMPARE_NO_BUY = "PRODUCT_COMPARE_NO_BUY";

    private static final Set<String> VALID_EVENTS = Set.of(
            CART_ABANDON,
            PAGE_EXIT,
            LONG_STAY_NO_ORDER,
            SEARCH_NO_CLICK,
            PRODUCT_COMPARE_NO_BUY
    );

    private static final Set<String> VALID_STRATEGIES = Set.of(
            "CART_REMINDER",
            "EXIT_RETENTION",
            "DECISION_ASSIST",
            "SEARCH_ASSIST",
            "COMPARE_ASSIST"
    );

    private final UserTool userTool;
    private final ProductTool productTool;
    private final OrderTool orderTool;
    private final DeepSeekService deepSeekService;
    private final RetentionPrompt retentionPrompt;
    private final ChatMemoryService chatMemoryService;
    private final ObjectMapper objectMapper;
    private final Map<String, Instant> lastTriggerTimes = new ConcurrentHashMap<>();

    public RetentionAgent(UserTool userTool,
                          ProductTool productTool,
                          OrderTool orderTool,
                          DeepSeekService deepSeekService,
                          RetentionPrompt retentionPrompt,
                          ChatMemoryService chatMemoryService,
                          ObjectMapper objectMapper) {
        this.userTool = userTool;
        this.productTool = productTool;
        this.orderTool = orderTool;
        this.deepSeekService = deepSeekService;
        this.retentionPrompt = retentionPrompt;
        this.chatMemoryService = chatMemoryService;
        this.objectMapper = objectMapper;
    }

    public RetentionResponse generate(RetentionRequest request) {
        String sessionId = chatMemoryService.normalizeSessionId(request == null ? null : request.getSessionId(), "retention-");
        String eventType = normalizeEventType(request == null ? null : request.getEventType());
        long start = System.currentTimeMillis();
        boolean duplicateLimited = false;
        boolean couponAvailable = false;
        boolean deepSeekCalled = false;
        boolean fallback = false;
        int candidateCount = 0;

        try {
            String validationError = validateRequest(request, eventType);
            if (validationError != null) {
                return RetentionResponse.fail(sessionId, eventType, validationError);
            }

            String duplicateKey = duplicateKey(request, eventType);
            if (isDuplicateTrigger(duplicateKey)) {
                duplicateLimited = true;
                log.info("RetentionAgent duplicate limited, userId={}, eventType={}, productId={}",
                        request.getUserId(), eventType, request.getProductId());
                return RetentionResponse.builder()
                        .success(true)
                        .sessionId(sessionId)
                        .eventType(eventType)
                        .strategyType(fallbackStrategyType(eventType))
                        .message("近期已生成过挽留提示，本次不重复推送。")
                        .products(List.of())
                        .recommendedProducts(List.of())
                        .couponAvailable(false)
                        .fallback(true)
                        .build();
            }

            UserToolResponse profile = userTool.getUserProfile(request.getUserId());
            UserToolResponse behavior = safeCall(() -> userTool.getUserBehaviorSummary(request.getUserId()), null);
            List<String> coupons = safeList(safeCall(() -> userTool.getUserCouponList(request.getUserId()), List.of()));
            List<String> preference = safeList(safeCall(() -> userTool.getUserPreference(request.getUserId()), List.of()));
            List<OrderToolResponse> recentOrders = safeList(safeCall(() -> orderTool.getUserOrders(request.getUserId(), 10), List.of()));

            couponAvailable = hasRealCoupon(coupons);
            ProductToolResponse targetProduct = loadTargetProduct(request, eventType);
            List<ProductToolResponse> candidates = buildCandidates(request, eventType, targetProduct, recentOrders);
            candidateCount = candidates.size();

            RetentionStrategyResult fallbackStrategy = fallbackStrategy(eventType, targetProduct, couponAvailable);
            RetentionStrategyResult strategy;
            try {
                deepSeekCalled = true;
                strategy = generateByDeepSeek(eventType, request, profile, behavior, preference, coupons, recentOrders, targetProduct, candidates);
            } catch (Exception e) {
                fallback = true;
                log.warn("RetentionAgent DeepSeek fallback, userId={}, eventType={}, error={}",
                        request.getUserId(), eventType, e.getMessage());
                strategy = fallbackStrategy;
            }

            SanitizedStrategy sanitized = sanitizeStrategy(strategy, fallbackStrategy, eventType, candidates, couponAvailable);
            List<RetentionProductResult> products = toProductResults(sanitized.productIds(), candidates, eventType);

            chatMemoryService.append(sessionId, eventType + ":" + safe(request.getMessageContext()), sanitized.message());
            markTriggered(duplicateKey);

            log.info("RetentionAgent success, userId={}, eventType={}, productId={}, duplicateLimited={}, couponAvailable={}, candidateCount={}, deepSeekCalled={}, fallback={}, elapsedMs={}",
                    request.getUserId(), eventType, request.getProductId(), duplicateLimited, couponAvailable,
                    candidateCount, deepSeekCalled, fallback || !Boolean.TRUE.equals(strategy.getAiGenerated()),
                    System.currentTimeMillis() - start);

            return RetentionResponse.builder()
                    .success(true)
                    .sessionId(sessionId)
                    .eventType(eventType)
                    .strategyType(sanitized.strategyType())
                    .message(sanitized.message())
                    .couponAvailable(couponAvailable)
                    .couponText(couponAvailable ? buildCouponText(coupons.get(0)) : null)
                    .products(products)
                    .recommendedProducts(toLegacyProducts(products, candidates))
                    .fallback(fallback || !Boolean.TRUE.equals(strategy.getAiGenerated()))
                    .build();
        } catch (AiException e) {
            log.warn("RetentionAgent handled error, userId={}, eventType={}, error={}",
                    request == null ? null : request.getUserId(), eventType, e.getMessage());
            return RetentionResponse.fail(sessionId, eventType, e.getMessage());
        } catch (Exception e) {
            log.error("RetentionAgent failed, userId={}, eventType={}",
                    request == null ? null : request.getUserId(), eventType, e);
            return RetentionResponse.fail(sessionId, eventType, "流失挽留生成失败，请稍后重试。");
        }
    }

    private String validateRequest(RetentionRequest request, String eventType) {
        if (request == null) {
            return "请求不能为空。";
        }
        if (request.getUserId() == null) {
            return "userId 不能为空。";
        }
        if (!StringUtils.hasText(eventType)) {
            return "eventType 不能为空。";
        }
        if (!VALID_EVENTS.contains(eventType)) {
            return "非法 eventType：" + eventType;
        }
        if (Set.of(CART_ABANDON, PAGE_EXIT, LONG_STAY_NO_ORDER).contains(eventType) && request.getProductId() == null) {
            return eventType + " 场景 productId 不能为空。";
        }
        if (PRODUCT_COMPARE_NO_BUY.equals(eventType)
                && (request.getComparedProductIds() == null || request.getComparedProductIds().size() < 2)) {
            return "PRODUCT_COMPARE_NO_BUY 场景 comparedProductIds 至少包含 2 个商品。";
        }
        return null;
    }

    private ProductToolResponse loadTargetProduct(RetentionRequest request, String eventType) {
        if (request.getProductId() == null) {
            return null;
        }
        try {
            return productTool.getProductDetail(request.getProductId());
        } catch (Exception e) {
            if (Set.of(CART_ABANDON, PAGE_EXIT, LONG_STAY_NO_ORDER).contains(eventType)) {
                throw e;
            }
            log.warn("RetentionAgent ignored missing target product, productId={}, error={}", request.getProductId(), e.getMessage());
            return null;
        }
    }

    private List<ProductToolResponse> buildCandidates(RetentionRequest request,
                                                      String eventType,
                                                      ProductToolResponse targetProduct,
                                                      List<OrderToolResponse> recentOrders) {
        Map<Long, ProductToolResponse> candidates = new LinkedHashMap<>();
        if (PRODUCT_COMPARE_NO_BUY.equals(eventType)) {
            for (Long productId : safeList(request.getComparedProductIds())) {
                addCandidate(candidates, safeCall(() -> productTool.getProductDetail(productId), null));
            }
        } else {
            addCandidate(candidates, targetProduct);
            for (ProductToolResponse product : safeList(safeCall(() -> productTool.getDiscountProducts(6), List.of()))) {
                addCandidate(candidates, product);
            }
            for (ProductToolResponse product : safeList(safeCall(() -> productTool.getHotProducts(6), List.of()))) {
                addCandidate(candidates, product);
            }
        }

        return candidates.values().stream()
                .filter(this::isPurchasable)
                .sorted(Comparator.comparingInt((ProductToolResponse product) -> candidateScore(product, targetProduct, recentOrders)).reversed())
                .limit(6)
                .toList();
    }

    private int candidateScore(ProductToolResponse product,
                               ProductToolResponse targetProduct,
                               List<OrderToolResponse> recentOrders) {
        int score = 0;
        if (targetProduct != null && product.getProductId() != null && product.getProductId().equals(targetProduct.getProductId())) {
            score += 50;
        }
        if (product.getStock() != null && product.getStock() > 0) {
            score += 10;
        }
        if (product.getSales() != null) {
            score += Math.min(product.getSales() / 20, 20);
        }
        if (StringUtils.hasText(product.getDiscountInfo()) && !product.getDiscountInfo().contains("暂无")) {
            score += 10;
        }
        if (hasPurchasedProduct(product.getProductId(), recentOrders)) {
            score -= 30;
        }
        return score;
    }

    private boolean hasPurchasedProduct(Long productId, List<OrderToolResponse> recentOrders) {
        if (productId == null) {
            return false;
        }
        for (OrderToolResponse order : safeList(recentOrders)) {
            for (OrderItemToolResponse item : safeList(order.getItems())) {
                if (productId.equals(item.getProductId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addCandidate(Map<Long, ProductToolResponse> candidates, ProductToolResponse product) {
        if (product == null || product.getProductId() == null || candidates.containsKey(product.getProductId())) {
            return;
        }
        candidates.put(product.getProductId(), product);
    }

    private boolean isPurchasable(ProductToolResponse product) {
        return product != null && product.getProductId() != null && product.getStock() != null && product.getStock() > 0;
    }

    private RetentionStrategyResult generateByDeepSeek(String eventType,
                                                       RetentionRequest request,
                                                       UserToolResponse profile,
                                                       UserToolResponse behavior,
                                                       List<String> preference,
                                                       List<String> coupons,
                                                       List<OrderToolResponse> recentOrders,
                                                       ProductToolResponse targetProduct,
                                                       List<ProductToolResponse> candidates) throws Exception {
        String prompt = buildPrompt(eventType, request, profile, behavior, preference, coupons, recentOrders, targetProduct, candidates);
        AiChatResponse response = deepSeekService.chat(retentionPrompt.systemPrompt(), prompt);
        if (!Boolean.TRUE.equals(response.getSuccess()) || !StringUtils.hasText(response.getContent())) {
            throw new AiException(502, "DeepSeek 挽留话术生成失败");
        }
        RetentionStrategyResult result = parseAiJson(response.getContent());
        result.setAiGenerated(true);
        return result;
    }

    private String buildPrompt(String eventType,
                               RetentionRequest request,
                               UserToolResponse profile,
                               UserToolResponse behavior,
                               List<String> preference,
                               List<String> coupons,
                               List<OrderToolResponse> recentOrders,
                               ProductToolResponse targetProduct,
                               List<ProductToolResponse> candidates) {
        return """
                请只返回 JSON：{"strategyType":"","message":"","recommendedProductIds":[],"couponAvailable":false}
                事件类型：%s
                事件上下文：%s
                用户画像：%s
                用户行为摘要：%s
                用户偏好：%s
                真实优惠券列表：%s
                最近订单：%s
                当前商品：%s
                系统候选商品：%s
                可用 strategyType：CART_REMINDER、EXIT_RETENTION、DECISION_ASSIST、SEARCH_ASSIST、COMPARE_ASSIST。
                recommendedProductIds 只能从系统候选商品 ID 中选择。
                如果真实优惠券列表为空，couponAvailable 必须为 false，message 禁止出现优惠券、满减、立减、赠品、折扣。
                """.formatted(eventType, safe(request.getMessageContext()), profile, behavior, preference, coupons,
                recentOrders, targetProduct, candidates);
    }

    private RetentionStrategyResult parseAiJson(String content) throws Exception {
        JsonNode node = objectMapper.readTree(extractJson(content));
        return RetentionStrategyResult.builder()
                .strategyType(textOrNull(node, "strategyType"))
                .message(textOrNull(node, "message"))
                .recommendedProductIds(idArray(node, "recommendedProductIds"))
                .couponAvailable(node.hasNonNull("couponAvailable") && node.get("couponAvailable").asBoolean(false))
                .aiGenerated(true)
                .build();
    }

    private SanitizedStrategy sanitizeStrategy(RetentionStrategyResult strategy,
                                               RetentionStrategyResult fallback,
                                               String eventType,
                                               List<ProductToolResponse> candidates,
                                               boolean couponAvailable) {
        String strategyType = VALID_STRATEGIES.contains(strategy.getStrategyType())
                ? strategy.getStrategyType()
                : fallbackStrategyType(eventType);
        String message = StringUtils.hasText(strategy.getMessage()) ? strategy.getMessage().trim() : fallback.getMessage();
        if (!couponAvailable && containsPromotionClaim(message)) {
            message = fallback.getMessage();
        }

        List<Long> allowedIds = candidates.stream().map(ProductToolResponse::getProductId).toList();
        List<Long> productIds = safeList(strategy.getRecommendedProductIds()).stream()
                .filter(allowedIds::contains)
                .distinct()
                .limit(3)
                .toList();
        if (productIds.isEmpty()) {
            productIds = candidates.stream().map(ProductToolResponse::getProductId).limit(3).toList();
        }

        return new SanitizedStrategy(strategyType, message, productIds);
    }

    private RetentionStrategyResult fallbackStrategy(String eventType,
                                                     ProductToolResponse targetProduct,
                                                     boolean couponAvailable) {
        return RetentionStrategyResult.builder()
                .strategyType(fallbackStrategyType(eventType))
                .message(fallbackMessage(eventType, targetProduct))
                .recommendedProductIds(List.of())
                .couponAvailable(couponAvailable)
                .aiGenerated(false)
                .build();
    }

    private String fallbackStrategyType(String eventType) {
        return switch (eventType) {
            case CART_ABANDON -> "CART_REMINDER";
            case PAGE_EXIT -> "EXIT_RETENTION";
            case LONG_STAY_NO_ORDER -> "DECISION_ASSIST";
            case SEARCH_NO_CLICK -> "SEARCH_ASSIST";
            case PRODUCT_COMPARE_NO_BUY -> "COMPARE_ASSIST";
            default -> "SEARCH_ASSIST";
        };
    }

    private String fallbackMessage(String eventType, ProductToolResponse targetProduct) {
        boolean hasStock = targetProduct != null && targetProduct.getStock() != null && targetProduct.getStock() > 0;
        return switch (eventType) {
            case CART_ABANDON -> hasStock
                    ? "你加入购物车的商品还在，可以继续完成下单。"
                    : "你关注的商品当前库存不足，可以看看其他相似商品。";
            case PAGE_EXIT -> "还有商品没有看完，可以继续浏览更多商城好物。";
            case LONG_STAY_NO_ORDER -> "如果你还在比较，可以使用 AI 导购查看商品差异和推荐理由。";
            case SEARCH_NO_CLICK -> "暂时没有找到合适商品，可以调整关键词或查看热销商品。";
            case PRODUCT_COMPARE_NO_BUY -> "可以根据价格、库存和销量选择更适合你的商品。";
            default -> "可以继续浏览更多适合你的商品。";
        };
    }

    private List<RetentionProductResult> toProductResults(List<Long> productIds,
                                                          List<ProductToolResponse> candidates,
                                                          String eventType) {
        Map<Long, ProductToolResponse> candidateMap = new LinkedHashMap<>();
        for (ProductToolResponse product : candidates) {
            candidateMap.put(product.getProductId(), product);
        }
        List<RetentionProductResult> results = new ArrayList<>();
        for (Long productId : safeList(productIds)) {
            ProductToolResponse product = candidateMap.get(productId);
            if (!isPurchasable(product)) {
                continue;
            }
            results.add(RetentionProductResult.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .imageUrl(product.getImageUrl())
                    .reason(buildProductReason(product, eventType))
                    .build());
            if (results.size() >= 3) {
                break;
            }
        }
        return results;
    }

    private List<ProductToolResponse> toLegacyProducts(List<RetentionProductResult> products, List<ProductToolResponse> candidates) {
        Map<Long, ProductToolResponse> candidateMap = new LinkedHashMap<>();
        for (ProductToolResponse product : candidates) {
            candidateMap.put(product.getProductId(), product);
        }
        return products.stream()
                .map(product -> candidateMap.get(product.getProductId()))
                .filter(product -> product != null)
                .toList();
    }

    private String buildProductReason(ProductToolResponse product, String eventType) {
        if (CART_ABANDON.equals(eventType)) {
            return "这是你刚刚关注的商品，当前仍有库存。";
        }
        if (PRODUCT_COMPARE_NO_BUY.equals(eventType)) {
            return "来自你对比过的真实商品，可按价格和库存继续比较。";
        }
        if (product.getSales() != null && product.getSales() > 0) {
            return "该商品销量为 " + product.getSales() + "，可作为参考。";
        }
        return "来自系统真实商品库的候选商品。";
    }

    private boolean isDuplicateTrigger(String key) {
        Instant last = lastTriggerTimes.get(key);
        return last != null && Duration.between(last, Instant.now()).compareTo(DUPLICATE_WINDOW) < 0;
    }

    private void markTriggered(String key) {
        lastTriggerTimes.put(key, Instant.now());
    }

    private String duplicateKey(RetentionRequest request, String eventType) {
        String productPart = request.getProductId() == null
                ? String.valueOf(safeList(request.getComparedProductIds()))
                : String.valueOf(request.getProductId());
        return request.getUserId() + ":" + eventType + ":" + productPart;
    }

    private boolean hasRealCoupon(List<String> coupons) {
        return coupons != null && coupons.stream().anyMatch(StringUtils::hasText);
    }

    private String buildCouponText(String coupon) {
        if (!StringUtils.hasText(coupon)) {
            return null;
        }
        String text = coupon.trim();
        if (text.length() > 30) {
            text = text.substring(0, 30);
        }
        return "你有可用优惠：" + text;
    }

    private boolean containsPromotionClaim(String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }
        return message.contains("优惠券")
                || message.contains("优惠")
                || message.contains("满减")
                || message.contains("立减")
                || message.contains("折扣")
                || message.contains("赠品")
                || message.contains("领券")
                || message.contains("券包");
    }

    private String extractJson(String content) {
        if (!StringUtils.hasText(content)) {
            throw new AiException(502, "DeepSeek 返回内容为空");
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json", "")
                    .replaceFirst("^```JSON", "")
                    .replaceFirst("^```", "")
                    .replaceFirst("```$", "")
                    .trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new AiException(502, "DeepSeek 返回 JSON 解析失败");
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text : null;
    }

    private List<Long> idArray(JsonNode node, String field) {
        JsonNode array = node.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (JsonNode item : array) {
            if (item.canConvertToLong()) {
                ids.add(item.asLong());
            }
        }
        return ids;
    }

    private String normalizeEventType(String eventType) {
        return StringUtils.hasText(eventType) ? eventType.trim().toUpperCase(Locale.ROOT) : null;
    }

    private <T> T safeCall(Supplier<T> supplier, T fallback) {
        try {
            T result = supplier.get();
            return result == null ? fallback : result;
        } catch (Exception e) {
            log.warn("RetentionAgent optional tool call ignored, error={}", e.getMessage());
            return fallback;
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record SanitizedStrategy(String strategyType, String message, List<Long> productIds) {
    }
}
