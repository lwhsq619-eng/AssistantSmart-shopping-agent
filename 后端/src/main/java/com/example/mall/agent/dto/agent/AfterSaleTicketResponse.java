package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * 售后工单智能分拣响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AfterSaleTicketResponse {
    private Boolean success;
    private String ticketType;
    private String priority;
    private String riskLevel;
    private String reason;
    private AfterSaleAdvice advice;
    private String message;

    public static AfterSaleTicketResponse fail(String message) {
        return AfterSaleTicketResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
