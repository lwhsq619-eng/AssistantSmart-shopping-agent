package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 个性化推荐商品项，商品字段来自 ProductTool，reason 由 AI 或规则生成。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationProductItem {
    private Long productId;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String imageUrl;
    private String discountInfo;
    private Double score;
    private String reason;
}
