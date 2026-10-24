package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 流失挽留推荐商品 DTO，只包含真实商品数据和 Java/AI 生成的推荐理由。
 */
@Data
@Builder
public class RetentionProductResult {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String reason;
}
