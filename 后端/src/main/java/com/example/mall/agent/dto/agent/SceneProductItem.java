package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 场景搭配商品项，商品基础信息来自 ProductTool。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SceneProductItem {
    private Long productId;
    private String productName;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String imageUrl;
    private String discountInfo;
    private String matchReason;
}
