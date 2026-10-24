package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 商品对比返回 DTO。
 */
@Data
@Builder
public class ProductCompareResponse {
    private List<ProductToolResponse> products;
    private String summary;
}
