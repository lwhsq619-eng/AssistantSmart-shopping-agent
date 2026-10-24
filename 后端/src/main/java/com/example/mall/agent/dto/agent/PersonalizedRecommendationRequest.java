package com.example.mall.agent.dto.agent;

import lombok.Data;

/**
 * 个性化推荐请求 DTO。
 */
@Data
public class PersonalizedRecommendationRequest {
    private Long userId;
    private String scene;
    private Integer limit;
}
