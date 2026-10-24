package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单数据分析请求 DTO。
 */
@Data
public class OrderDataAnalysisRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> analysisTypes;
    private Long categoryId;
}
