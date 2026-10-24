package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 从用户自然语言中抽取出的导购查询条件。
 */
@Data
@Builder
public class GuideQuery {
    private String keyword;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String usageScene;
    private String brand;
    private List<String> preferenceTags;
    private Boolean compareIntent;
    private Boolean hotIntent;
    private Boolean discountIntent;
    private Boolean afterSaleIntent;
    private List<Long> compareProductIds;
}
