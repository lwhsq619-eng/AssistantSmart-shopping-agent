package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 语义搜索商品结果 DTO，只包装 ProductTool 返回的真实商品数据。
 */
@Data
@Builder
public class SearchProductResult {
    private Long productId;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String imageUrl;
    private Double matchScore;
    private String matchReason;
}
