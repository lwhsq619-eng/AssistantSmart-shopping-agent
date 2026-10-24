package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.OperationBriefingRequest;
import com.example.mall.agent.dto.agent.OperationBriefingResponse;
import com.example.mall.agent.dto.agent.OperationBriefingSection;
import com.example.mall.agent.dto.agent.OperationMetricSummary;
import com.example.mall.agent.dto.tool.CommentSummaryResponse;
import com.example.mall.agent.dto.tool.InventoryToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.OperationBriefingPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.CommentTool;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 自动化运营简报 Agent。
 * Java 侧计算核心运营指标，DeepSeek 只负责总结表达和建议生成。
 */
@Service
public class OperationBriefingAgent {
    private static final Logger log = LoggerFactory.getLogger(OperationBriefingAgent.class);
    private static final Set<String> BRIEFING_TYPES = Set.of("DAILY", "WEEKLY", "MONTHLY", "CUSTOM");
    private static final Set<String> SECTION_TYPES = Set.of(
            "REVENUE",
            "ORDER",
            "CUSTOMER_SERVICE",
            "AFTER_SALE",
            "INVENTORY",
            "COMMENT",
            "FULL"
    );

    private final OrderTool orderTool;
    private final ProductTool productTool;
    private final UserTool userTool;
    private final InventoryTool inventoryTool;
    private final CommentTool commentTool;
    private final AiService aiService;
    private final OperationBriefingPrompt operationBriefingPrompt;
    private final ObjectMapper objectMapper;

    public OperationBriefingAgent(OrderTool orderTool,
                                  ProductTool productTool,
                                  UserTool userTool,
                                  InventoryTool inventoryTool,
                                  CommentTool commentTool,
                                  AiService aiService,
                                  OperationBriefingPrompt operationBriefingPrompt,
                                  ObjectMapper objectMapper) {
        this.orderTool = orderTool;
        this.productTool = productTool;
        this.userTool = userTool;
        this.inventoryTool = inventoryTool;
        this.commentTool = commentTool;
        this.aiService = aiService;
        this.operationBriefingPrompt = operationBriefingPrompt;
        this.objectMapper = objectMapper;
    }

    public OperationBriefingResponse generate(OperationBriefingRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return OperationBriefingResponse.fail(
                    request == null ? null : request.getStartDate(),
                    request == null ? null : request.getEndDate(),
                    validationError);
        }

        String briefingType = request.getBriefingType().trim().toUpperCase();
        List<String> sections = normalizeSections(request.getSections());
        long start = System.currentTimeMillis();
        try {
            List<OrderToolResponse> orders = safeOrders(request);
            List<ProductToolResponse> hotProducts = safeHotProducts();
            List<InventoryToolResponse> lowStockProducts = safeLowStockProducts();
            List<InventoryToolResponse> slowMovingProducts = safeSlowMovingProducts(request);
            CommentSnapshot commentSnapshot = buildCommentSnapshot(hotProducts);
            OperationMetricSummary metricSummary = buildMetricSummary(orders, lowStockProducts, slowMovingProducts, commentSnapshot);

            BriefingAiResult aiResult = generateAiBriefing(request, briefingType, sections, metricSummary,
                    hotProducts, lowStockProducts, slowMovingProducts, commentSnapshot, orders);
            log.info("OperationBriefingAgent success, startDate={}, endDate={}, type={}, elapsedMs={}",
                    request.getStartDate(), request.getEndDate(), briefingType, System.currentTimeMillis() - start);
            return OperationBriefingResponse.builder()
                    .success(true)
                    .briefingType(briefingType)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .title(aiResult.title())
                    .metricSummary(metricSummary)
                    .sections(aiResult.sections())
                    .suggestions(aiResult.suggestions())
                    .riskWarnings(aiResult.riskWarnings())
                    .reportMarkdown(aiResult.reportMarkdown())
                    .message("简报生成完成，核心指标均由 Java 基于系统真实数据计算。")
                    .build();
        } catch (Exception e) {
            log.error("OperationBriefingAgent failed, startDate={}, endDate={}", request.getStartDate(), request.getEndDate(), e);
            return OperationBriefingResponse.fail(request.getStartDate(), request.getEndDate(), "运营简报生成失败：" + e.getMessage());
        }
    }

    private OperationMetricSummary buildMetricSummary(List<OrderToolResponse> orders,
                                                      List<InventoryToolResponse> lowStockProducts,
                                                      List<InventoryToolResponse> slowMovingProducts,
                                                      CommentSnapshot commentSnapshot) {
        int totalOrders = orders.size();
        int paidOrders = (int) orders.stream().filter(this::isPaidOrder).count();
        int cancelledOrders = (int) orders.stream().filter(this::isCancelledOrder).count();
        int refundedOrders = (int) orders.stream().filter(this::isRefundedOrder).count();
        BigDecimal totalSalesAmount = orders.stream()
                .filter(this::isPaidOrder)
                .map(OrderToolResponse::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageOrderValue = paidOrders == 0 ? BigDecimal.ZERO
                : totalSalesAmount.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);
        BigDecimal refundRate = totalOrders == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(refundedOrders).divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP);
        int afterSaleIssueCount = refundedOrders;

        return OperationMetricSummary.builder()
                .totalOrders(totalOrders)
                .paidOrders(paidOrders)
                .cancelledOrders(cancelledOrders)
                .refundedOrders(refundedOrders)
                .totalSalesAmount(totalSalesAmount)
                .averageOrderValue(averageOrderValue)
                .refundRate(refundRate)
                .lowStockProductCount(lowStockProducts.size())
                .slowMovingProductCount(slowMovingProducts.size())
                .negativeCommentCount(commentSnapshot.negativeCount())
                .positiveCommentCount(commentSnapshot.positiveCount())
                .customerServiceIssueCount(0)
                .afterSaleIssueCount(afterSaleIssueCount)
                .build();
    }

    private BriefingAiResult generateAiBriefing(OperationBriefingRequest request,
                                                String briefingType,
                                                List<String> sections,
                                                OperationMetricSummary metricSummary,
                                                List<ProductToolResponse> hotProducts,
                                                List<InventoryToolResponse> lowStockProducts,
                                                List<InventoryToolResponse> slowMovingProducts,
                                                CommentSnapshot commentSnapshot,
                                                List<OrderToolResponse> orders) {
        BriefingAiResult fallback = fallbackBriefing(request, briefingType, sections, metricSummary);
        try {
            String prompt = buildPrompt(request, briefingType, sections, metricSummary, hotProducts,
                    lowStockProducts, slowMovingProducts, commentSnapshot, orders);
            AiChatResponse aiResponse = shouldUseReasoner(briefingType, sections)
                    ? aiService.reason(operationBriefingPrompt.systemPrompt(), prompt)
                    : aiService.chat(operationBriefingPrompt.systemPrompt(), prompt);
            if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
                return fallback;
            }
            return parseAiResult(aiResponse.getContent(), fallback);
        } catch (Exception e) {
            log.warn("OperationBriefingAgent AI fallback, error={}", e.getMessage());
            return fallback;
        }
    }

    private String buildPrompt(OperationBriefingRequest request,
                               String briefingType,
                               List<String> sections,
                               OperationMetricSummary metricSummary,
                               List<ProductToolResponse> hotProducts,
                               List<InventoryToolResponse> lowStockProducts,
                               List<InventoryToolResponse> slowMovingProducts,
                               CommentSnapshot commentSnapshot,
                               List<OrderToolResponse> orders) {
        return "请只返回 JSON，格式：{\"title\":\"\",\"sections\":[{\"sectionType\":\"REVENUE\",\"title\":\"\",\"content\":\"\"}],"
                + "\"suggestions\":[],\"riskWarnings\":[],\"reportMarkdown\":\"\"}\n"
                + "简报类型：" + briefingType + "\n"
                + "简报范围：" + request.getStartDate() + " 至 " + request.getEndDate() + "\n"
                + "需要板块：" + sections + "\n"
                + "Java 已计算核心指标：" + metricSummary + "\n"
                + "热销商品样本：" + hotProducts + "\n"
                + "低库存商品：" + lowStockProducts + "\n"
                + "滞销商品：" + slowMovingProducts + "\n"
                + "评论快照：" + commentSnapshot + "\n"
                + "订单样本数量：" + orders.size() + "\n"
                + "客服咨询数据：项目当前无独立客服会话统计，customerServiceIssueCount=0 表示暂无数据。\n"
                + "要求：所有结论必须基于输入指标；缺失模块必须写暂无数据；不得编造营收、订单、库存、评论、售后数据。";
    }

    private BriefingAiResult parseAiResult(String content, BriefingAiResult fallback) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(content));
        String title = textOrDefault(root, "title", fallback.title());
        List<OperationBriefingSection> sections = parseSections(root.get("sections"));
        List<String> suggestions = stringArray(root, "suggestions");
        List<String> riskWarnings = stringArray(root, "riskWarnings");
        String markdown = textOrDefault(root, "reportMarkdown", fallback.reportMarkdown());
        return new BriefingAiResult(
                title,
                sections.isEmpty() ? fallback.sections() : sections,
                suggestions.isEmpty() ? fallback.suggestions() : suggestions,
                riskWarnings.isEmpty() ? fallback.riskWarnings() : riskWarnings,
                markdown);
    }

    private BriefingAiResult fallbackBriefing(OperationBriefingRequest request,
                                              String briefingType,
                                              List<String> requestedSections,
                                              OperationMetricSummary metricSummary) {
        String title = request.getStartDate() + " 至 " + request.getEndDate() + " 运营简报";
        List<OperationBriefingSection> sections = new ArrayList<>();
        for (String section : expandSections(requestedSections)) {
            sections.add(fallbackSection(section, metricSummary));
        }
        List<String> suggestions = List.of(
                "关注低库存商品，及时安排补货或调整活动资源。",
                "围绕支付订单和客单价变化优化活动门槛。",
                "售后和评论模块暂无独立完整数据时，建议结合人工记录复核。"
        );
        List<String> risks = List.of(
                "客服咨询数据暂无独立统计，相关结论仅标记为暂无数据。",
                "评论数据取决于 CommentTool 当前返回结果，若为空则代表暂无可分析评论数据。"
        );
        return new BriefingAiResult(title, sections, suggestions, risks, buildFallbackMarkdown(title, metricSummary, sections));
    }

    private OperationBriefingSection fallbackSection(String section, OperationMetricSummary metricSummary) {
        return switch (section) {
            case "REVENUE" -> OperationBriefingSection.builder()
                    .sectionType("REVENUE")
                    .title("营收概览")
                    .content("本周期支付订单数为 " + metricSummary.getPaidOrders()
                            + "，销售额为 " + metricSummary.getTotalSalesAmount()
                            + "，客单价为 " + metricSummary.getAverageOrderValue() + "。")
                    .build();
            case "ORDER" -> OperationBriefingSection.builder()
                    .sectionType("ORDER")
                    .title("订单概览")
                    .content("本周期总订单数为 " + metricSummary.getTotalOrders()
                            + "，取消订单数为 " + metricSummary.getCancelledOrders()
                            + "，退款订单数为 " + metricSummary.getRefundedOrders() + "。")
                    .build();
            case "CUSTOMER_SERVICE" -> OperationBriefingSection.builder()
                    .sectionType("CUSTOMER_SERVICE")
                    .title("客服咨询")
                    .content("当前项目暂无独立客服会话统计，客服问题数记为 " + metricSummary.getCustomerServiceIssueCount() + "。")
                    .build();
            case "AFTER_SALE" -> OperationBriefingSection.builder()
                    .sectionType("AFTER_SALE")
                    .title("售后问题")
                    .content("售后问题数按退款/退货相关订单估算为 " + metricSummary.getAfterSaleIssueCount()
                            + "，建议客服及时跟进。")
                    .build();
            case "INVENTORY" -> OperationBriefingSection.builder()
                    .sectionType("INVENTORY")
                    .title("库存风险")
                    .content("低库存商品数为 " + metricSummary.getLowStockProductCount()
                            + "，滞销商品数为 " + metricSummary.getSlowMovingProductCount() + "。")
                    .build();
            case "COMMENT" -> OperationBriefingSection.builder()
                    .sectionType("COMMENT")
                    .title("评论反馈")
                    .content("负面评论数为 " + metricSummary.getNegativeCommentCount()
                            + "，正面评论数为 " + metricSummary.getPositiveCommentCount()
                            + "，建议重点关注负面反馈。")
                    .build();
            default -> OperationBriefingSection.builder()
                    .sectionType(section)
                    .title(section)
                    .content("暂无数据。")
                    .build();
        };
    }

    private String buildFallbackMarkdown(String title, OperationMetricSummary metricSummary, List<OperationBriefingSection> sections) {
        StringBuilder builder = new StringBuilder("## ").append(title).append("\n\n");
        builder.append("### 核心指标\n")
                .append("- 总订单数：").append(metricSummary.getTotalOrders()).append("\n")
                .append("- 支付订单数：").append(metricSummary.getPaidOrders()).append("\n")
                .append("- 销售额：").append(metricSummary.getTotalSalesAmount()).append("\n")
                .append("- 客单价：").append(metricSummary.getAverageOrderValue()).append("\n")
                .append("- 退款率：").append(metricSummary.getRefundRate()).append("\n")
                .append("- 低库存商品数：").append(metricSummary.getLowStockProductCount()).append("\n")
                .append("- 负面评论数：").append(metricSummary.getNegativeCommentCount()).append("\n\n");
        for (OperationBriefingSection section : sections) {
            builder.append("### ").append(section.getTitle()).append("\n")
                    .append(section.getContent()).append("\n\n");
        }
        return builder.toString();
    }

    private List<OrderToolResponse> safeOrders(OperationBriefingRequest request) {
        try {
            return orderTool.getOrdersByDateRange(request.getStartDate(), request.getEndDate(), null);
        } catch (Exception e) {
            log.warn("OperationBriefing orders unavailable, error={}", e.getMessage());
            return List.of();
        }
    }

    private List<ProductToolResponse> safeHotProducts() {
        try {
            return productTool.getHotProducts(5);
        } catch (Exception e) {
            log.warn("OperationBriefing hot products unavailable, error={}", e.getMessage());
            return List.of();
        }
    }

    private List<InventoryToolResponse> safeLowStockProducts() {
        try {
            return inventoryTool.getLowStockProducts(10);
        } catch (Exception e) {
            log.warn("OperationBriefing low stock unavailable, error={}", e.getMessage());
            return List.of();
        }
    }

    private List<InventoryToolResponse> safeSlowMovingProducts(OperationBriefingRequest request) {
        try {
            int days = Math.max(1, (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1);
            return inventoryTool.getSlowMovingProducts(Math.min(days, 365), 20);
        } catch (Exception e) {
            log.warn("OperationBriefing slow moving unavailable, error={}", e.getMessage());
            return List.of();
        }
    }

    private CommentSnapshot buildCommentSnapshot(List<ProductToolResponse> products) {
        int positive = 0;
        int negative = 0;
        for (ProductToolResponse product : products) {
            try {
                CommentSummaryResponse summary = commentTool.getCommentSummary(product.getProductId());
                positive += summary.getPositiveCount() == null ? 0 : summary.getPositiveCount();
                negative += summary.getNegativeCount() == null ? 0 : summary.getNegativeCount();
            } catch (Exception e) {
                log.warn("OperationBriefing comment summary skipped, productId={}, error={}", product.getProductId(), e.getMessage());
            }
        }
        return new CommentSnapshot(positive, negative);
    }

    private String validateRequest(OperationBriefingRequest request) {
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
        if (!StringUtils.hasText(request.getBriefingType())) {
            return "briefingType 不能为空。";
        }
        if (!BRIEFING_TYPES.contains(request.getBriefingType().trim().toUpperCase())) {
            return "briefingType 不合法。";
        }
        if (request.getSections() == null || request.getSections().isEmpty()) {
            return "sections 不能为空。";
        }
        if (normalizeSections(request.getSections()).isEmpty()) {
            return "sections 不合法。";
        }
        return null;
    }

    private List<String> normalizeSections(List<String> sections) {
        Set<String> result = new LinkedHashSet<>();
        for (String section : sections) {
            if (!StringUtils.hasText(section)) {
                continue;
            }
            String normalized = section.trim().toUpperCase();
            if (SECTION_TYPES.contains(normalized)) {
                result.add(normalized);
            }
        }
        return new ArrayList<>(result);
    }

    private List<String> expandSections(List<String> sections) {
        if (sections.contains("FULL")) {
            return List.of("REVENUE", "ORDER", "CUSTOMER_SERVICE", "AFTER_SALE", "INVENTORY", "COMMENT");
        }
        return sections;
    }

    private boolean shouldUseReasoner(String briefingType, List<String> sections) {
        return "MONTHLY".equals(briefingType)
                || "CUSTOM".equals(briefingType)
                || sections.contains("FULL")
                || sections.size() >= 4;
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

    private List<OperationBriefingSection> parseSections(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<OperationBriefingSection> result = new ArrayList<>();
        for (JsonNode item : node) {
            result.add(OperationBriefingSection.builder()
                    .sectionType(textOrDefault(item, "sectionType", "UNKNOWN"))
                    .title(textOrDefault(item, "title", "未命名板块"))
                    .content(textOrDefault(item, "content", "暂无数据。"))
                    .build());
        }
        return result;
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

    private record CommentSnapshot(int positiveCount, int negativeCount) {
    }

    private record BriefingAiResult(String title,
                                    List<OperationBriefingSection> sections,
                                    List<String> suggestions,
                                    List<String> riskWarnings,
                                    String reportMarkdown) {
    }
}
