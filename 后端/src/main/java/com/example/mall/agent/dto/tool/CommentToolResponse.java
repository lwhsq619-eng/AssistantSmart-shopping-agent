package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论 Tool 返回 DTO，避免 AI 模块直接暴露评论实体。
 */
@Data
@Builder
public class CommentToolResponse {
    private Long commentId;
    private Long productId;
    private Long userId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
    private String sentiment;
    private String replyStatus;
}
