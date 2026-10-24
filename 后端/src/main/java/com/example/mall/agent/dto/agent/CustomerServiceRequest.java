package com.example.mall.agent.dto.agent;

import lombok.Data;

/**
 * 全天候智能客服请求 DTO。
 */
@Data
public class CustomerServiceRequest {
    private Long userId;
    private String sessionId;
    private String message;
}
