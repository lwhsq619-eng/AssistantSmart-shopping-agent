package com.example.mall.agent.dto.agent;

import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单数据分析响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDataAnalysisResponse {
    private Boolean success;
    private LocalDate startDate;
    private LocalDate endDate;
    private OrderMetricSummary metricSummary;
    private List<ProductToolResponse> hotProducts;
    private String consumerBehaviorSummary;
    private List<String> hotProductAnalysis;
    private BusinessAdviceResult businessAdvice;
    private String reportMarkdown;
    private String message;

    public static OrderDataAnalysisResponse fail(LocalDate startDate, LocalDate endDate, String message) {
        return OrderDataAnalysisResponse.builder()
                .success(false)
                .startDate(startDate)
                .endDate(endDate)
                .message(message)
                .build();
    }
}
