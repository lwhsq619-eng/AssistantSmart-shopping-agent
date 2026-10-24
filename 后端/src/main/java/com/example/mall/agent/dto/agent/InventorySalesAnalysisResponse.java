package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存销售分析响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventorySalesAnalysisResponse {
    private Boolean success;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<InventoryMetricItem> metrics;
    private InventoryAdviceResult advice;
    private String reportMarkdown;
    private String message;

    public static InventorySalesAnalysisResponse fail(LocalDate startDate, LocalDate endDate, String message) {
        return InventorySalesAnalysisResponse.builder()
                .success(false)
                .startDate(startDate)
                .endDate(endDate)
                .metrics(List.of())
                .message(message)
                .build();
    }
}
