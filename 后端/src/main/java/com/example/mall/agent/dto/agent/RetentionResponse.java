package com.example.mall.agent.dto.agent;

import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 流失用户挽留响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetentionResponse {
    private Boolean success;
    private String sessionId;
    private String eventType;
    private String strategyType;
    private String message;
    private Boolean couponAvailable;
    private String couponText;
    private List<RetentionProductResult> products;
    private Boolean fallback;
    private String errorMessage;

    /**
     * 兼容早期响应字段，后续前端建议使用 products。
     */
    private List<ProductToolResponse> recommendedProducts;

    public static RetentionResponse fail(String sessionId, String eventType, String errorMessage) {
        return RetentionResponse.builder()
                .success(false)
                .sessionId(sessionId)
                .eventType(eventType)
                .message(errorMessage)
                .products(List.of())
                .recommendedProducts(List.of())
                .couponAvailable(false)
                .fallback(true)
                .errorMessage(errorMessage)
                .build();
    }
}
