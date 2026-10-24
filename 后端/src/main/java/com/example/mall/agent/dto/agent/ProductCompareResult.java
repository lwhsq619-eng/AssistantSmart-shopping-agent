package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品对比结果 DTO。
 */
@Data
@Builder
public class ProductCompareResult {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String advantage;
    private String risk;
}
