package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 商品评论摘要报表 DTO。
 */
@Data
@Builder
public class CommentSummaryReport {
    private Long productId;
    private String productName;
    private Integer totalCount;
    private Map<String, Integer> sentimentSummary;
    private List<String> negativeProblems;
    private String summaryReport;
}
