package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 导购推荐商品 DTO，只来自 ProductTool 返回的数据。
 */
@Data
@Builder
public class ProductRecommendResult {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String discountInfo;
    private String reason;
}
