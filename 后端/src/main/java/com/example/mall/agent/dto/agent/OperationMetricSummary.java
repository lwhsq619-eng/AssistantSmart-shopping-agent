package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运营简报核心指标 DTO，所有字段由 Java 侧计算。
 */
@Data
@Builder
public class OperationMetricSummary {
    private Integer totalOrders;
    private Integer paidOrders;
    private Integer cancelledOrders;
    private Integer refundedOrders;
    private BigDecimal totalSalesAmount;
    private BigDecimal averageOrderValue;
    private BigDecimal refundRate;
    private Integer lowStockProductCount;
    private Integer slowMovingProductCount;
    private Integer negativeCommentCount;
    private Integer positiveCommentCount;
    private Integer customerServiceIssueCount;
    private Integer afterSaleIssueCount;
}
