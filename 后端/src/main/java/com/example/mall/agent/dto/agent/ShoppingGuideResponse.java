package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 智能导购响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShoppingGuideResponse {
    private Boolean success;
    private String sessionId;
    private String intent;
    private String reply;
    private List<RecommendedProductDTO> products;
    private Boolean needMoreInfo;
    private String message;

    public static ShoppingGuideResponse fail(String sessionId, String message) {
        return ShoppingGuideResponse.builder()
                .success(false)
                .sessionId(sessionId)
                .message(message)
                .products(List.of())
                .needMoreInfo(true)
                .build();
    }
}
