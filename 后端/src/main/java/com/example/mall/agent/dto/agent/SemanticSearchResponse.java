package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 语义化商品搜索响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SemanticSearchResponse {
    private Boolean success;
    private String query;
    private String normalizedKeyword;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> sceneTags;
    private List<String> functionTags;
    private List<String> tags;
    private String sortBy;
    private List<SearchProductResult> products;
    private Boolean fallback;
    private Boolean fallbackUsed;
    private String message;
    private Integer page;
    private Integer size;
    private Integer total;

    public static SemanticSearchResponse fail(String query, String message) {
        return SemanticSearchResponse.builder()
                .success(false)
                .query(query)
                .message(message)
                .products(List.of())
                .sceneTags(List.of())
                .functionTags(List.of())
                .tags(List.of())
                .fallback(true)
                .fallbackUsed(true)
                .page(1)
                .size(10)
                .total(0)
                .build();
    }
}
