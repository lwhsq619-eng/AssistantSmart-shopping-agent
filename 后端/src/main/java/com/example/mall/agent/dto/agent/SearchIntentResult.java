package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 语义搜索意图解析结果，只描述检索条件，不包含任何商品结果。
 */
@Data
@Builder
public class SearchIntentResult {
    private String normalizedKeyword;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> sceneTags;
    private List<String> functionTags;
    private List<String> excludedTags;
    private String sortBy;
    private Boolean aiParsed;
}
