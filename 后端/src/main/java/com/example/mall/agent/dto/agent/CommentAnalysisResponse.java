package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 评论智能分析响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentAnalysisResponse {
    private Boolean success;
    private Long productId;
    private Map<String, Integer> sentimentSummary;
    private List<CommentReplySuggestion> replySuggestions;
    private List<String> negativeProblems;
    private String summaryReport;
    private CommentSummaryReport report;
    private String message;

    public static CommentAnalysisResponse fail(Long productId, String message) {
        return CommentAnalysisResponse.builder()
                .success(false)
                .productId(productId)
                .message(message)
                .replySuggestions(List.of())
                .negativeProblems(List.of())
                .build();
    }
}
