package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品 Tool 返回 DTO，避免直接暴露商品 Entity。
 */
@Data
@Builder
public class ProductToolResponse {
    private Long productId;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String description;
    private String imageUrl;
    private String discountInfo;
}
