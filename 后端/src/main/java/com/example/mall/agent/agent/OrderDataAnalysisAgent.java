package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.BusinessAdviceResult;
import com.example.mall.agent.dto.agent.OrderDataAnalysisRequest;
import com.example.mall.agent.dto.agent.OrderDataAnalysisResponse;
import com.example.mall.agent.dto.agent.OrderMetricSummary;
import com.example.mall.agent.dto.tool.InventoryToolResponse;
import com.example.mall.agent.dto.tool.OrderItemToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.prompt.OrderDataAnalysisPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.InventoryTool;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户订单数据分析 Agent。
 * Java 侧计算所有核心指标，DeepSeek 只基于真实指标生成经营分析文本。
 */
@Service
public class OrderDataAnalysisAgent {
    private static final Logger log = LoggerFactory.getLogger(OrderDataAnalysisAgent.class);
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "CONSUMER_BEHAVIOR",
            "HOT_PRODUCTS",
            "REPURCHASE",
            "PRICE",
            "ACTIVITY",
            "FULL_REPORT"
    );

    private final OrderTool orderTool;
    private final UserTool userTool;
    private final ProductTool productTool;
    private final InventoryTool inventoryTool;
    private final AiService aiService;
    private final OrderDataAnalysisPrompt orderDataAnalysisPrompt;
    private final ObjectMapper objectMapper;

    public OrderDataAnalysisAgent(OrderTool orderTool,
                                  UserTool userTool,
                                  ProductTool productTool,
                                  InventoryTool inventoryTool,
                                  AiService aiService,
                                  OrderDataAnalysisPrompt orderDataAnalysisPrompt,
                                  ObjectMapper objectMapper) {
        this.orderTool = orderTool;
        this.userTool = userTool;
        this.productTool = productTool;
        this.inventoryTool = inventoryTool;
        this.aiService = aiService;
        this.orderDataAnalysisPrompt = orderDataAnalysisPrompt;
        this.objectMapper = objectMapper;
    }

    public OrderDataAnalysisResponse analyze(OrderDataAnalysisRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return OrderDataAnalysisResponse.fail(
                    request == null ? null : request.getStartDate(),
                    request == null ? null : request.getEndDate(),
                    validationError);
        }

        long start = System.currentTimeMillis();
        List<String> analysisTypes = normalizeTypes(request.getAnalysisTypes());
        try {
            List<OrderToolResponse> orders = orderTool.getOrdersByDateRange(
                    request.getStartDate(), request.getEndDate(), request.getCategoryId());
            if (orders.isEmpty()) {
                return OrderDataAnalysisResponse.fail(request.getStartDate(), request.getEndDate(), "指定时间范围内没有订单数据。");
            }

            MetricContext context = buildMetricContext(orders);
            OrderMetricSummary metricSummary = buildMetricSummary(context);
            List<ProductToolResponse> hotProducts = loadHotProducts(context);
            List<InventoryToolResponse> inventorySignals = loadInventorySignals(hotProducts);
            List<UserToolResponse> userSamples = loadUserSamples(context);
            AiAnalysisResult aiResult = generateAiAnalysis(request, analysisTypes, metricSummary, hotProducts, inventorySignals, userSamples, orders);

            log.info("OrderDataAnalysisAgent success, startDate={}, endDate={}, elapsedMs={}, orders={}",
                    request.getStartDate(), request.getEndDate(), System.currentTimeMillis() - start, orders.size());
            return OrderDataAnalysisResponse.builder()
                    .success(true)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .metricSummary(metricSummary)
                    .hotProducts(hotProducts)
                    .consumerBehaviorSummary(aiResult.consumerBehaviorSummary())
                    .hotProductAnalysis(aiResult.hotProductAnalysis())
                    .businessAdvice(aiResult.businessAdvice())
                    .reportMarkdown(aiResult.reportMarkdown())
                    .message("分析完成，所有指标均基于系统订单和商品数据计算。")
                    .build();
        } catch (Exception e) {
            log.error("OrderDataAnalysisAgent failed, startDate={}, endDate={}", request.getStartDate(), request.getEndDate(), e);
            return OrderDataAnalysisResponse.fail(request.getStartDate(), request.getEndDate(), "订单数据分析失败：" + e.getMessage());
        }
    }

    private MetricContext buildMetricContext(List<OrderToolResponse> orders) {
        List<OrderToolResponse> paidOrders = orders.stream().filter(this::isPaidOrder).toList();
        int cancelledOrders = (int) orders.stream().filter(this::isCancelledOrder).count();
        int refundedOrders = (int) orders.stream().filter(this::isRefundedOrder).count();
        BigDecimal totalSalesAmount = paidOrders.stream()
                .map(OrderToolResponse::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Long, Integer> paidOrdersByUser = new HashMap<>();
        Map<Long, ProductAgg> productAggMap = new LinkedHashMap<>();
        Map<String, Integer> categoryQuantityMap = new LinkedHashMap<>();

        for (OrderToolResponse order : paidOrders) {
            paidOrdersByUser.merge(order.getUserId(), 1, Integer::sum);
            for (OrderItemToolResponse item : safeItems(order)) {
                if (item.getProductId() == null) {
                    continue;
                }
                ProductAgg agg = productAggMap.computeIfAbsent(item.getProductId(),
                        key -> new ProductAgg(item.getProductId(), item.getProductName()));
                int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                BigDecimal subtotal = item.getSubtotal() == null ? BigDecimal.ZERO : item.getSubtotal();
                agg.quantity += quantity;
                agg.amount = agg.amount.add(subtotal);
                ProductToolResponse product = safeProductDetail(item.getProductId());
                if (product != null && StringUtils.hasText(product.getCategoryName())) {
                    categoryQuantityMap.merge(product.getCategoryName(), quantity, Integer::sum);
                }
            }
        }
        return new MetricContext(orders, paidOrders, cancelledOrders, refundedOrders, totalSalesAmount,
                paidOrdersByUser, productAggMap, categoryQuantityMap);
    }

    private OrderMetricSummary buildMetricSummary(MetricContext context) {
        int paidOrders = context.paidOrders().size();
        int paidUserCount = context.paidOrdersByUser().size();
        int repurchaseUserCount = (int) context.paidOrdersByUser().values().stream().filter(count -> count >= 2).count();
        BigDecimal averageOrderValue = paidOrders == 0 ? BigDecimal.ZERO
                : context.totalSalesAmount().divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);
        BigDecimal repurchaseRate = paidUserCount == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(repurchaseUserCount).divide(BigDecimal.valueOf(paidUserCount), 4, RoundingMode.HALF_UP);
        BigDecimal refundRate = context.orders().isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(context.refundedOrders()).divide(BigDecimal.valueOf(context.orders().size()), 4, RoundingMode.HALF_UP);
        return OrderMetricSummary.builder()
                .totalOrders(context.orders().size())
                .paidOrders(paidOrders)
                .cancelledOrders(context.cancelledOrders())
                .refundedOrders(context.refundedOrders())
                .totalSalesAmount(context.totalSalesAmount())
                .averageOrderValue(averageOrderValue)
                .paidUserCount(paidUserCount)
                .repurchaseUserCount(repurchaseUserCount)
                .repurchaseRate(repurchaseRate)
                .refundRate(refundRate)
                .topCategories(topCategoryNames(context.categoryQuantityMap()))
                .topProducts(topProductNames(context.productAggMap()))
                .build();
    }

    private List<ProductToolResponse> loadHotProducts(MetricContext context) {
        List<Long> productIds = context.productAggMap().values().stream()
                .sorted(Comparator.comparingInt(ProductAgg::quantity).reversed())
                .map(ProductAgg::productId)
                .limit(5)
                .toList();
        List<ProductToolResponse> result = new ArrayList<>();
        for (Long productId : productIds) {
            ProductToolResponse product = safeProductDetail(productId);
            if (product != null) {
                result.add(product);
            }
        }
        if (result.isEmpty()) {
            result.addAll(productTool.getHotProducts(5));
        }
        return result;
    }

    private List<InventoryToolResponse> loadInventorySignals(List<ProductToolResponse> hotProducts) {
        List<InventoryToolResponse> result = new ArrayList<>();
        for (ProductToolResponse product : hotProducts) {
            try {
                result.add(inventoryTool.getSalesAndStock(product.getProductId()));
            } catch (Exception e) {
                log.warn("Inventory signal skipped, productId={}, error={}", product.getProductId(), e.getMessage());
            }
        }
        return result;
    }

    private AiAnalysisResult generateAiAnalysis(OrderDataAnalysisRequest request,
                                                List<String> analysisTypes,
                                                OrderMetricSummary metricSummary,
                                                List<ProductToolResponse> hotProducts,
                                                List<InventoryToolResponse> inventorySignals,
                                                List<UserToolResponse> userSamples,
                                                List<OrderToolResponse> orders) {
        AiAnalysisResult fallback = fallbackAnalysis(metricSummary, hotProducts);
        try {
            String prompt = buildPrompt(request, analysisTypes, metricSummary, hotProducts, inventorySignals, userSamples, orders);
            AiChatResponse aiResponse = shouldUseReasoner(analysisTypes)
                    ? aiService.reason(orderDataAnalysisPrompt.systemPrompt(), prompt)
                    : aiService.chat(orderDataAnalysisPrompt.systemPrompt(), prompt);
            if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
                return fallback;
            }
            return parseAiResult(aiResponse.getContent(), fallback);
        } catch (Exception e) {
            log.warn("OrderDataAnalysisAgent AI fallback, error={}", e.getMessage());
            return fallback;
        }
    }

    private String buildPrompt(OrderDataAnalysisRequest request,
                               List<String> analysisTypes,
                               OrderMetricSummary metricSummary,
                               List<ProductToolResponse> hotProducts,
                               List<InventoryToolResponse> inventorySignals,
                               List<UserToolResponse> userSamples,
                               List<OrderToolResponse> orders) {
        return "请只返回 JSON，格式：{\"consumerBehaviorSummary\":\"\",\"hotProductAnalysis\":[],"
                + "\"businessAdvice\":{\"selectionAdvice\":[],\"pricingAdvice\":[],\"activityAdvice\":[],\"riskWarnings\":[]},"
                + "\"reportMarkdown\":\"\"}\n"
                + "分析范围：" + request.getStartDate() + " 至 " + request.getEndDate() + "\n"
                + "分析类型：" + analysisTypes + "\n"
                + "Java 已计算指标：" + metricSummary + "\n"
                + "热销商品真实数据：" + hotProducts + "\n"
                + "库存信号：" + inventorySignals + "\n"
                + "用户行为样本：" + userSamples + "\n"
                + "订单样本数量：" + orders.size() + "\n"
                + "要求：所有结论必须引用上述指标，不得编造订单、销量、营收、库存。";
    }

    private List<UserToolResponse> loadUserSamples(MetricContext context) {
        List<UserToolResponse> result = new ArrayList<>();
        for (Long userId : context.paidOrdersByUser().keySet().stream().limit(5).toList()) {
            try {
                result.add(userTool.getUserBehaviorSummary(userId));
            } catch (Exception e) {
                log.warn("User behavior sample skipped, userId={}, error={}", userId, e.getMessage());
            }
        }
        return result;
    }

    private AiAnalysisResult parseAiResult(String content, AiAnalysisResult fallback) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(content));
        String consumerSummary = textOrDefault(root, "consumerBehaviorSummary", fallback.consumerBehaviorSummary());
        List<String> hotAnalysis = stringArray(root, "hotProductAnalysis");
        BusinessAdviceResult advice = parseBusinessAdvice(root.get("businessAdvice"), fallback.businessAdvice());
        String markdown = textOrDefault(root, "reportMarkdown", fallback.reportMarkdown());
        return new AiAnalysisResult(
                consumerSummary,
                hotAnalysis.isEmpty() ? fallback.hotProductAnalysis() : hotAnalysis,
                advice,
                markdown);
    }

    private BusinessAdviceResult parseBusinessAdvice(JsonNode node, BusinessAdviceResult fallback) {
        if (node == null || node.isNull()) {
            return fallback;
        }
        List<String> selection = stringArray(node, "selectionAdvice");
        List<String> pricing = stringArray(node, "pricingAdvice");
        List<String> activity = stringArray(node, "activityAdvice");
        List<String> risks = stringArray(node, "riskWarnings");
        return BusinessAdviceResult.builder()
                .selectionAdvice(selection.isEmpty() ? fallback.getSelectionAdvice() : selection)
                .pricingAdvice(pricing.isEmpty() ? fallback.getPricingAdvice() : pricing)
                .activityAdvice(activity.isEmpty() ? fallback.getActivityAdvice() : activity)
                .riskWarnings(risks.isEmpty() ? fallback.getRiskWarnings() : risks)
                .build();
    }

    private AiAnalysisResult fallbackAnalysis(OrderMetricSummary metricSummary, List<ProductToolResponse> hotProducts) {
        BusinessAdviceResult advice = BusinessAdviceResult.builder()
                .selectionAdvice(List.of("优先关注销量高、库存不足的商品。"))
                .pricingAdvice(List.of("结合客单价和热销商品价格区间进行活动定价。"))
                .activityAdvice(List.of("围绕热销品类设计满减、组合购活动。"))
                .riskWarnings(List.of("当前为规则分析结果，建议结合更多时间周期复核。"))
                .build();
        String summary = "本期订单数 " + metricSummary.getTotalOrders()
                + "，支付订单 " + metricSummary.getPaidOrders()
                + "，销售额 " + metricSummary.getTotalSalesAmount()
                + "，客单价 " + metricSummary.getAverageOrderValue()
                + "，复购率 " + metricSummary.getRepurchaseRate() + "。";
        String markdown = "## 订单分析报告\n\n"
                + "- 总订单数：" + metricSummary.getTotalOrders() + "\n"
                + "- 支付订单数：" + metricSummary.getPaidOrders() + "\n"
                + "- 销售额：" + metricSummary.getTotalSalesAmount() + "\n"
                + "- 客单价：" + metricSummary.getAverageOrderValue() + "\n"
                + "- 复购率：" + metricSummary.getRepurchaseRate() + "\n"
                + "- 热销商品：" + hotProducts.stream().map(ProductToolResponse::getProductName).toList() + "\n\n"
                + "### 建议\n"
                + "优先关注销量高、库存不足的商品；结合客单价和热销商品价格区间进行活动定价。";
        return new AiAnalysisResult(
                summary,
                hotProducts.stream().map(product -> product.getProductName() + "销量和库存表现需结合库存信号评估。").toList(),
                advice,
                markdown);
    }

    private String validateRequest(OrderDataAnalysisRequest request) {
        if (request == null || request.getStartDate() == null) {
            return "startDate 不能为空。";
        }
        if (request.getEndDate() == null) {
            return "endDate 不能为空。";
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "startDate 不能晚于 endDate。";
        }
        if (ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) > 366) {
            return "日期范围不能超过 366 天。";
        }
        if (request.getAnalysisTypes() == null || request.getAnalysisTypes().isEmpty()) {
            return "analysisTypes 不能为空。";
        }
        if (normalizeTypes(request.getAnalysisTypes()).isEmpty()) {
            return "analysisTypes 不合法。";
        }
        return null;
    }

    private List<String> normalizeTypes(List<String> types) {
        Set<String> result = new LinkedHashSet<>();
        for (String type : types) {
            if (!StringUtils.hasText(type)) {
                continue;
            }
            String normalized = type.trim().toUpperCase();
            if (ALLOWED_TYPES.contains(normalized)) {
                result.add(normalized);
            }
        }
        return new ArrayList<>(result);
    }

    private boolean shouldUseReasoner(List<String> analysisTypes) {
        return analysisTypes.contains("FULL_REPORT")
                || analysisTypes.contains("PRICE")
                || analysisTypes.contains("ACTIVITY")
                || analysisTypes.size() >= 3;
    }

    private boolean isPaidOrder(OrderToolResponse order) {
        String status = safe(order.getStatus());
        return !status.contains("待支付") && !status.contains("已取消") && !status.contains("取消");
    }

    private boolean isCancelledOrder(OrderToolResponse order) {
        String status = safe(order.getStatus());
        return status.contains("已取消") || status.contains("取消");
    }

    private boolean isRefundedOrder(OrderToolResponse order) {
        String status = safe(order.getStatus());
        return status.contains("退款") || status.contains("退货") || status.contains("已退款");
    }

    private List<OrderItemToolResponse> safeItems(OrderToolResponse order) {
        return order.getItems() == null ? List.of() : order.getItems();
    }

    private ProductToolResponse safeProductDetail(Long productId) {
        try {
            return productTool.getProductDetail(productId);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> topCategoryNames(Map<String, Integer> categoryQuantityMap) {
        return categoryQuantityMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
                .toList();
    }

    private List<String> topProductNames(Map<Long, ProductAgg> productAggMap) {
        return productAggMap.values().stream()
                .sorted(Comparator.comparingInt(ProductAgg::quantity).reversed())
                .limit(5)
                .map(agg -> agg.productName() + "(" + agg.quantity() + ")")
                .toList();
    }

    private String extractJson(String content) {
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !StringUtils.hasText(value.asText())) {
            return defaultValue;
        }
        return value.asText();
    }

    private List<String> stringArray(JsonNode node, String field) {
        if (node == null) {
            return List.of();
        }
        JsonNode array = node.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : array) {
            if (StringUtils.hasText(item.asText())) {
                values.add(item.asText());
            }
        }
        return values;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record MetricContext(List<OrderToolResponse> orders,
                                 List<OrderToolResponse> paidOrders,
                                 int cancelledOrders,
                                 int refundedOrders,
                                 BigDecimal totalSalesAmount,
                                 Map<Long, Integer> paidOrdersByUser,
                                 Map<Long, ProductAgg> productAggMap,
                                 Map<String, Integer> categoryQuantityMap) {
    }

    private static final class ProductAgg {
        private final Long productId;
        private final String productName;
        private int quantity;
        private BigDecimal amount = BigDecimal.ZERO;

        private ProductAgg(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        private Long productId() {
            return productId;
        }

        private String productName() {
            return productName;
        }

        private int quantity() {
            return quantity;
        }
    }

    private record AiAnalysisResult(String consumerBehaviorSummary,
                                    List<String> hotProductAnalysis,
                                    BusinessAdviceResult businessAdvice,
                                    String reportMarkdown) {
    }
}
