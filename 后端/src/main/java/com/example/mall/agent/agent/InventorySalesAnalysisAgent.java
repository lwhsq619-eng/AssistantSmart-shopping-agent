package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.InventoryAdviceResult;
import com.example.mall.agent.dto.agent.InventoryMetricItem;
import com.example.mall.agent.dto.agent.InventorySalesAnalysisRequest;
import com.example.mall.agent.dto.agent.InventorySalesAnalysisResponse;
import com.example.mall.agent.dto.tool.InventoryToolResponse;
import com.example.mall.agent.dto.tool.OrderItemToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.InventorySalesAnalysisPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.InventoryTool;
import com.example.mall.agent.tool.OrderTool;
import com.example.mall.agent.tool.ProductTool;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 库存销售分析 Agent。
 * 关键库存指标在 Java 侧计算，DeepSeek 只负责解释和建议生成。
 */
@Service
public class InventorySalesAnalysisAgent {
    private static final Logger log = LoggerFactory.getLogger(InventorySalesAnalysisAgent.class);
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "REPLENISHMENT",
            "LOW_STOCK",
            "SLOW_MOVING",
            "CLEARANCE",
            "FULL_REPORT"
    );

    private final InventoryTool inventoryTool;
    private final ProductTool productTool;
    private final OrderTool orderTool;
    private final AiService aiService;
    private final InventorySalesAnalysisPrompt inventorySalesAnalysisPrompt;
    private final ObjectMapper objectMapper;

    public InventorySalesAnalysisAgent(InventoryTool inventoryTool,
                                       ProductTool productTool,
                                       OrderTool orderTool,
                                       AiService aiService,
                                       InventorySalesAnalysisPrompt inventorySalesAnalysisPrompt,
                                       ObjectMapper objectMapper) {
        this.inventoryTool = inventoryTool;
        this.productTool = productTool;
        this.orderTool = orderTool;
        this.aiService = aiService;
        this.inventorySalesAnalysisPrompt = inventorySalesAnalysisPrompt;
        this.objectMapper = objectMapper;
    }

    public InventorySalesAnalysisResponse analyze(InventorySalesAnalysisRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return InventorySalesAnalysisResponse.fail(
                    request == null ? null : request.getStartDate(),
                    request == null ? null : request.getEndDate(),
                    validationError);
        }

        long start = System.currentTimeMillis();
        List<String> analysisTypes = normalizeTypes(request.getAnalysisTypes());
        try {
            List<OrderToolResponse> orders = orderTool.getOrdersByDateRange(request.getStartDate(), request.getEndDate(), request.getCategoryId());
            List<Long> productIds = resolveProductIds(request, orders);
            if (productIds.isEmpty()) {
                return InventorySalesAnalysisResponse.fail(request.getStartDate(), request.getEndDate(), "未找到可分析商品。");
            }

            List<InventoryMetricItem> metrics = new ArrayList<>();
            for (Long productId : productIds) {
                ProductToolResponse product = productTool.getProductDetail(productId);
                InventoryToolResponse inventory = inventoryTool.getSalesAndStock(productId);
                metrics.add(buildMetric(product, inventory));
            }
            if (metrics.isEmpty()) {
                return InventorySalesAnalysisResponse.fail(request.getStartDate(), request.getEndDate(), "库存数据为空。");
            }

            InventoryAiResult aiResult = generateAiAnalysis(request, analysisTypes, metrics, orders);
            log.info("InventorySalesAnalysisAgent success, startDate={}, endDate={}, metricCount={}, elapsedMs={}",
                    request.getStartDate(), request.getEndDate(), metrics.size(), System.currentTimeMillis() - start);
            return InventorySalesAnalysisResponse.builder()
                    .success(true)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .metrics(metrics)
                    .advice(aiResult.advice())
                    .reportMarkdown(aiResult.reportMarkdown())
                    .message("分析完成，核心指标均由 Java 基于库存和销量数据计算。")
                    .build();
        } catch (Exception e) {
            log.error("InventorySalesAnalysisAgent failed, startDate={}, endDate={}", request.getStartDate(), request.getEndDate(), e);
            return InventorySalesAnalysisResponse.fail(request.getStartDate(), request.getEndDate(), "库存销售分析失败：" + e.getMessage());
        }
    }

    private InventoryMetricItem buildMetric(ProductToolResponse product, InventoryToolResponse inventory) {
        int stock = safeInt(inventory.getStock());
        int sales7d = safeInt(inventory.getSales7d());
        int sales30d = safeInt(inventory.getSales30d());
        BigDecimal averageDailySales = BigDecimal.valueOf(sales30d).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        BigDecimal estimatedDaysToSellOut = averageDailySales.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(stock).divide(averageDailySales, 2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(999999);
        BigDecimal stockTurnoverRate = stock > 0
                ? BigDecimal.valueOf(sales30d).divide(BigDecimal.valueOf(stock), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String riskLevel = resolveRiskLevel(estimatedDaysToSellOut);
        boolean slowMoving = sales30d == 0 || (stock > 0 && averageDailySales.compareTo(BigDecimal.valueOf(0.2)) < 0);
        int recommendedReplenishmentQty = "HIGH".equals(riskLevel)
                ? BigDecimal.valueOf(30).multiply(averageDailySales).subtract(BigDecimal.valueOf(stock)).setScale(0, RoundingMode.CEILING).max(BigDecimal.ZERO).intValue()
                : 0;
        return InventoryMetricItem.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .stock(stock)
                .sales7d(sales7d)
                .sales30d(sales30d)
                .averageDailySales(averageDailySales)
                .estimatedDaysToSellOut(estimatedDaysToSellOut)
                .stockTurnoverRate(stockTurnoverRate)
                .lowStockFlag("HIGH".equals(riskLevel) || "MEDIUM".equals(riskLevel))
                .slowMovingFlag(slowMoving)
                .stockRiskLevel(riskLevel)
                .recommendedReplenishmentQty(recommendedReplenishmentQty)
                .build();
    }

    private InventoryAiResult generateAiAnalysis(InventorySalesAnalysisRequest request,
                                                 List<String> analysisTypes,
                                                 List<InventoryMetricItem> metrics,
                                                 List<OrderToolResponse> orders) {
        InventoryAiResult fallback = fallbackAnalysis(metrics);
        try {
            String prompt = buildPrompt(request, analysisTypes, metrics, orders);
            AiChatResponse response = shouldUseReasoner(analysisTypes)
                    ? aiService.reason(inventorySalesAnalysisPrompt.systemPrompt(), prompt)
                    : aiService.chat(inventorySalesAnalysisPrompt.systemPrompt(), prompt);
            if (!Boolean.TRUE.equals(response.getSuccess()) || !StringUtils.hasText(response.getContent())) {
                return fallback;
            }
            return parseAiResult(response.getContent(), fallback);
        } catch (Exception e) {
            log.warn("InventorySalesAnalysisAgent AI fallback, error={}", e.getMessage());
            return fallback;
        }
    }

    private String buildPrompt(InventorySalesAnalysisRequest request,
                               List<String> analysisTypes,
                               List<InventoryMetricItem> metrics,
                               List<OrderToolResponse> orders) {
        return "请只返回 JSON，格式：{\"replenishmentAdvice\":[],\"lowStockWarnings\":[],"
                + "\"slowMovingProducts\":[],\"clearancePlan\":[],\"riskWarnings\":[],\"reportMarkdown\":\"\"}\n"
                + "分析范围：" + request.getStartDate() + " 至 " + request.getEndDate() + "\n"
                + "分析类型：" + analysisTypes + "\n"
                + "Java 已计算库存指标：" + metrics + "\n"
                + "订单样本数量：" + orders.size() + "\n"
                + "要求：所有建议必须基于库存、近7日销量、近30日销量、预计售罄天数；不得编造销量和库存。";
    }

    private InventoryAiResult parseAiResult(String content, InventoryAiResult fallback) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(content));
        InventoryAdviceResult advice = InventoryAdviceResult.builder()
                .replenishmentAdvice(nonEmptyArray(root, "replenishmentAdvice", fallback.advice().getReplenishmentAdvice()))
                .lowStockWarnings(nonEmptyArray(root, "lowStockWarnings", fallback.advice().getLowStockWarnings()))
                .slowMovingProducts(nonEmptyArray(root, "slowMovingProducts", fallback.advice().getSlowMovingProducts()))
                .clearancePlan(nonEmptyArray(root, "clearancePlan", fallback.advice().getClearancePlan()))
                .riskWarnings(nonEmptyArray(root, "riskWarnings", fallback.advice().getRiskWarnings()))
                .build();
        String report = textOrDefault(root, "reportMarkdown", fallback.reportMarkdown());
        return new InventoryAiResult(advice, report);
    }

    private InventoryAiResult fallbackAnalysis(List<InventoryMetricItem> metrics) {
        List<String> replenishmentAdvice = new ArrayList<>();
        List<String> lowStockWarnings = new ArrayList<>();
        List<String> slowMovingProducts = new ArrayList<>();
        List<String> clearancePlan = new ArrayList<>();
        List<String> riskWarnings = new ArrayList<>();

        for (InventoryMetricItem item : metrics) {
            if ("HIGH".equals(item.getStockRiskLevel())) {
                lowStockWarnings.add(item.getProductName() + "库存预计7天内售罄，建议及时补货。");
            } else if ("MEDIUM".equals(item.getStockRiskLevel())) {
                lowStockWarnings.add(item.getProductName() + "库存预计15天内售罄，建议关注销售变化。");
            }
            if (Boolean.TRUE.equals(item.getSlowMovingFlag())) {
                slowMovingProducts.add(item.getProductName() + "近30日销量较低，建议参与组合促销或清仓活动。");
                clearancePlan.add(item.getProductName() + "可与热销品组合搭配，降低单品滞销风险。");
            }
            if (item.getRecommendedReplenishmentQty() != null && item.getRecommendedReplenishmentQty() > 0) {
                replenishmentAdvice.add(item.getProductName() + "建议补货数量为 " + item.getRecommendedReplenishmentQty() + "。");
            }
        }
        if (replenishmentAdvice.isEmpty()) {
            replenishmentAdvice.add("当前未发现必须立即补货的商品。");
        }
        if (lowStockWarnings.isEmpty()) {
            lowStockWarnings.add("当前未发现高缺货风险商品。");
        }
        if (slowMovingProducts.isEmpty()) {
            slowMovingProducts.add("当前未发现明显滞销商品。");
        }
        if (clearancePlan.isEmpty()) {
            clearancePlan.add("暂无强制清仓需求，可持续观察库存周转变化。");
        }
        riskWarnings.add("当前为规则分析结果，建议结合实际采购周期和活动计划复核。");

        InventoryAdviceResult advice = InventoryAdviceResult.builder()
                .replenishmentAdvice(replenishmentAdvice)
                .lowStockWarnings(lowStockWarnings)
                .slowMovingProducts(slowMovingProducts)
                .clearancePlan(clearancePlan)
                .riskWarnings(riskWarnings)
                .build();
        return new InventoryAiResult(advice, buildFallbackMarkdown(metrics, advice));
    }

    private String buildFallbackMarkdown(List<InventoryMetricItem> metrics, InventoryAdviceResult advice) {
        StringBuilder builder = new StringBuilder("## 库存销售分析报告\n\n");
        builder.append("### 核心指标\n");
        for (InventoryMetricItem item : metrics) {
            builder.append("- ").append(item.getProductName())
                    .append("：库存 ").append(item.getStock())
                    .append("，近30日销量 ").append(item.getSales30d())
                    .append("，日均销量 ").append(item.getAverageDailySales())
                    .append("，预计售罄天数 ").append(item.getEstimatedDaysToSellOut())
                    .append("，风险 ").append(item.getStockRiskLevel())
                    .append("\n");
        }
        builder.append("\n### 补货建议\n").append(String.join("\n", advice.getReplenishmentAdvice()));
        builder.append("\n\n### 缺货预警\n").append(String.join("\n", advice.getLowStockWarnings()));
        builder.append("\n\n### 滞销与清仓\n").append(String.join("\n", advice.getSlowMovingProducts()));
        return builder.toString();
    }

    private List<Long> resolveProductIds(InventorySalesAnalysisRequest request, List<OrderToolResponse> orders) {
        Set<Long> ids = new LinkedHashSet<>();
        if (request.getProductIds() != null) {
            ids.addAll(request.getProductIds());
        }
        if (ids.isEmpty() && request.getCategoryId() != null) {
            for (OrderToolResponse order : orders) {
                if (order.getItems() == null) {
                    continue;
                }
                for (OrderItemToolResponse item : order.getItems()) {
                    if (item.getProductId() != null) {
                        ids.add(item.getProductId());
                    }
                }
            }
        }
        return ids.stream().limit(50).toList();
    }

    private String validateRequest(InventorySalesAnalysisRequest request) {
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
        boolean productIdsEmpty = request.getProductIds() == null || request.getProductIds().isEmpty();
        if (productIdsEmpty && request.getCategoryId() == null) {
            return "productIds 为空时 categoryId 不能为空。";
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
            if (Set.of("REPLENISHMENT", "LOW_STOCK", "SLOW_MOVING", "CLEARANCE", "FULL_REPORT").contains(normalized)) {
                result.add(normalized);
            }
        }
        return new ArrayList<>(result);
    }

    private boolean shouldUseReasoner(List<String> analysisTypes) {
        return analysisTypes.contains("FULL_REPORT")
                || analysisTypes.contains("CLEARANCE")
                || analysisTypes.size() >= 3;
    }

    private String resolveRiskLevel(BigDecimal estimatedDaysToSellOut) {
        if (estimatedDaysToSellOut.compareTo(BigDecimal.valueOf(7)) <= 0) {
            return "HIGH";
        }
        if (estimatedDaysToSellOut.compareTo(BigDecimal.valueOf(15)) <= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
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

    private List<String> nonEmptyArray(JsonNode node, String field, List<String> fallback) {
        List<String> values = stringArray(node, field);
        return values.isEmpty() ? fallback : values;
    }

    private List<String> stringArray(JsonNode node, String field) {
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

    private String textOrDefault(JsonNode node, String field, String fallback) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !StringUtils.hasText(value.asText())) {
            return fallback;
        }
        return value.asText();
    }

    private record InventoryAiResult(InventoryAdviceResult advice, String reportMarkdown) {
    }
}
