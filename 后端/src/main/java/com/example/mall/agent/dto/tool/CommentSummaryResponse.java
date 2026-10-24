package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品评论摘要 DTO，供客服和导购 Agent 后续生成回答使用。
 */
@Data
@Builder
public class CommentSummaryResponse {
    private Long productId;
    private Integer totalCount;
    private Integer positiveCount;
    private Integer negativeCount;
    private BigDecimal averageRating;
    private String summary;
}
