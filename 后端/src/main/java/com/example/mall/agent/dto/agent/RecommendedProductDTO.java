package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 导购推荐商品 DTO。
 * 只来自 ProductTool 返回的真实商品数据，不由大模型生成。
 */
@Data
@Builder
public class RecommendedProductDTO {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String reason;
}
