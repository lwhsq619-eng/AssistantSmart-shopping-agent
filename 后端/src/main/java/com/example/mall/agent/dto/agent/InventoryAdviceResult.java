package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 库存销售建议结果 DTO。
 */
@Data
@Builder
public class InventoryAdviceResult {
    private List<String> replenishmentAdvice;
    private List<String> lowStockWarnings;
    private List<String> slowMovingProducts;
    private List<String> clearancePlan;
    private List<String> riskWarnings;
}
