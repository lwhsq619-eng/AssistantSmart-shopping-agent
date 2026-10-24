package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存分析 Tool 返回 DTO，金额和统计字段保持结构化。
 */
@Data
@Builder
public class InventoryToolResponse {
    private Long productId;
    private String productName;
    private Integer stock;
    private Integer sales7d;
    private Integer sales30d;
    private BigDecimal averageDailySales;
    private BigDecimal estimatedDaysToSellOut;
    private String suggestion;
}
