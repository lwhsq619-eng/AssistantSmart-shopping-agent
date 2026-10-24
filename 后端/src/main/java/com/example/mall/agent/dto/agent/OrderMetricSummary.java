package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Java 侧计算的订单核心指标。
 */
@Data
@Builder
public class OrderMetricSummary {
    private Integer totalOrders;
    private Integer paidOrders;
    private Integer cancelledOrders;
    private Integer refundedOrders;
    private BigDecimal totalSalesAmount;
    private BigDecimal averageOrderValue;
    private Integer paidUserCount;
    private Integer repurchaseUserCount;
    private BigDecimal repurchaseRate;
    private BigDecimal refundRate;
    private List<String> topCategories;
    private List<String> topProducts;
}
