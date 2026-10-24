package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 场景化搭配推荐响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SceneMatchResponse {
    private Boolean success;
    private String sceneType;
    private BigDecimal budget;
    private BigDecimal totalPrice;
    private List<SceneMatchGroup> groups;
    private Boolean fallbackUsed;
    private String message;

    public static SceneMatchResponse fail(String sceneType, BigDecimal budget, String message) {
        return SceneMatchResponse.builder()
                .success(false)
                .sceneType(sceneType)
                .budget(budget)
                .totalPrice(BigDecimal.ZERO)
                .groups(List.of())
                .message(message)
                .build();
    }
}
