package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 售后建议 DTO，所有订单状态来自 OrderTool。
 */
@Data
@Builder
public class AfterSaleAdviceResult {
    private Long orderId;
    private String orderNo;
    private String status;
    private Boolean refundAvailable;
    private Boolean actionable;
    private Boolean needHuman;
    private String advice;
}
