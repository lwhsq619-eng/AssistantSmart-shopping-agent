package com.example.mall.agent.workflow;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 执行日志对象。
 * 第一版仅打印日志，不落库。
 */
@Data
@Builder
public class AgentExecutionLog {
    private String requestId;
    private Long userId;
    private String role;
    private String sessionId;
    private String intent;
    private String agentName;
    private Boolean success;
    private Long durationMs;
    private String errorMessage;
    private LocalDateTime createTime;
}
