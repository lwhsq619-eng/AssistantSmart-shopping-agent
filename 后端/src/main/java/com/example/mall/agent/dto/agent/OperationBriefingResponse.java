package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 自动化运营简报响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationBriefingResponse {
    private Boolean success;
    private String briefingType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private OperationMetricSummary metricSummary;
    private List<OperationBriefingSection> sections;
    private List<String> suggestions;
    private List<String> riskWarnings;
    private String reportMarkdown;
    private String message;

    public static OperationBriefingResponse fail(LocalDate startDate, LocalDate endDate, String message) {
        return OperationBriefingResponse.builder()
                .success(false)
                .startDate(startDate)
                .endDate(endDate)
                .sections(List.of())
                .message(message)
                .build();
    }
}
