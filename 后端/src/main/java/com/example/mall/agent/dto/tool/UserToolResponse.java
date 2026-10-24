package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户 Tool 返回 DTO。
 */
@Data
@Builder
public class UserToolResponse {
    private Long userId;
    private String username;
    private String level;
    private List<String> historyCategories;
    private List<OrderToolResponse> recentOrders;
    private List<ProductToolResponse> cartItems;
    private List<String> coupons;
    private List<String> preferenceTags;
}
