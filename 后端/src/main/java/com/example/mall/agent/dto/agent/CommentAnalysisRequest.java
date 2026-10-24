package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.util.List;

/**
 * 评论智能分析请求 DTO。
 */
@Data
public class CommentAnalysisRequest {
    private Long productId;
    private Integer limit;
    private List<String> analysisTypes;
}
