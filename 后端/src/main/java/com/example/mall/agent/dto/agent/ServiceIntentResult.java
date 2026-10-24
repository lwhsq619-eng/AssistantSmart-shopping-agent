package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 客服意图识别结果。
 */
@Data
@Builder
public class ServiceIntentResult {
    private String intent;
    private String orderNo;
    private Long orderId;
    private String keyword;
    private Boolean useReasoner;
    private Boolean needHuman;
    private String description;
}
