package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 评论回复建议 DTO。只供运营审核，不自动发布。
 */
@Data
@Builder
public class CommentReplySuggestion {
    private Long commentId;
    private String sentiment;
    private String suggestedReply;
    private String reason;
}
