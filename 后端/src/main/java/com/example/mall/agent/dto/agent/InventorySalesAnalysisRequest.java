package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存销售分析请求 DTO。
 */
@Data
public class InventorySalesAnalysisRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> productIds;
    private Long categoryId;
    private List<String> analysisTypes;
}
