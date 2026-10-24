package com.example.mall.agent.dto.agent;

import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 全天候智能客服响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerServiceResponse {
    private Boolean success;
    private String sessionId;
    private String intent;
    private String reply;
    private ServiceIntentResult intentResult;
    private AfterSaleAdviceResult afterSaleAdvice;
    private OrderToolResponse order;
    private List<OrderToolResponse> orders;
    private List<ProductToolResponse> products;
    private List<String> coupons;
    private String message;

    public static CustomerServiceResponse fail(String sessionId, String message) {
        return CustomerServiceResponse.builder()
                .success(false)
                .sessionId(sessionId)
                .message(message)
                .build();
    }
}
