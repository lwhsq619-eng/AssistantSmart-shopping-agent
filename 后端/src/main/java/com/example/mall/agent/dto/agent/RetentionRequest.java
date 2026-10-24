package com.example.mall.agent.dto.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 流失用户挽留请求 DTO。
 */
@Data
public class RetentionRequest {
    @NotNull(message = "userId 不能为空")
    private Long userId;

    private String sessionId;

    @NotBlank(message = "eventType 不能为空")
    private String eventType;

    private Long productId;
    private String messageContext;
    private List<Long> comparedProductIds;
}
