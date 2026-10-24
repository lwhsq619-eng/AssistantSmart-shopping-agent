package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 场景化搭配推荐请求 DTO。
 */
@Data
public class SceneMatchRequest {
    private Long userId;
    private String sceneType;
    private BigDecimal budget;
    private String message;
    private Integer limit;
}
