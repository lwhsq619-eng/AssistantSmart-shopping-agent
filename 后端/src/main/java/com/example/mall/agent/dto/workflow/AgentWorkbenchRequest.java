package com.example.mall.agent.dto.workflow;

import lombok.Data;

/**
 * AI 工作台统一请求 DTO。
 */
@Data
public class AgentWorkbenchRequest {
    private Long userId;
    private String role;
    private String sessionId;
    private String message;
}
