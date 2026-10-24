package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.util.List;

/**
 * 售后工单智能分拣请求 DTO。
 */
@Data
public class AfterSaleTicketRequest {
    private Long userId;
    private Long orderId;
    private String message;
    private List<String> images;
}
