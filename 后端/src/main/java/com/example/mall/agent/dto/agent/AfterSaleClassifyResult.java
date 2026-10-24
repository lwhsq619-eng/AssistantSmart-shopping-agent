package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 售后工单分拣结果 DTO。
 */
@Data
@Builder
public class AfterSaleClassifyResult {
    private String ticketType;
    private String priority;
    private String riskLevel;
    private String reason;
    private AfterSaleAdvice advice;
}
