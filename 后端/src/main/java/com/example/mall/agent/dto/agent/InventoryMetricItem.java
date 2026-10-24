package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Java 侧计算出的单品库存销售指标。
 */
@Data
@Builder
public class InventoryMetricItem {
    private Long productId;
    private String productName;
    private Integer stock;
    private Integer sales7d;
    private Integer sales30d;
    private BigDecimal averageDailySales;
    private BigDecimal estimatedDaysToSellOut;
    private BigDecimal stockTurnoverRate;
    private Boolean lowStockFlag;
    private Boolean slowMovingFlag;
    private String stockRiskLevel;
    private Integer recommendedReplenishmentQty;
}
