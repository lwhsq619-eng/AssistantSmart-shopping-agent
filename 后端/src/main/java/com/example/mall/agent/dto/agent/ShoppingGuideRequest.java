package com.example.mall.agent.dto.agent;

import lombok.Data;

/**
 * 智能导购请求 DTO。
 */
@Data
public class ShoppingGuideRequest {
    private Long userId;
    private String sessionId;
    private String message;
}
