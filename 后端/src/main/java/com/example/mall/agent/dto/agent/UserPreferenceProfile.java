package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 用户推荐画像 DTO，只汇总 Tool 返回的真实行为数据。
 */
@Data
@Builder
public class UserPreferenceProfile {
    private Long userId;
    private String username;
    private String level;
    private List<String> historyCategories;
    private List<String> preferenceTags;
    private List<String> coupons;
    private Set<Long> purchasedProductIds;
    private BigDecimal averagePurchasePrice;
    private Boolean behaviorDataInsufficient;
}
