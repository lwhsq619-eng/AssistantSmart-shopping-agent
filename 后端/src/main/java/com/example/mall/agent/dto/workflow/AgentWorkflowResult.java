package com.example.mall.agent.dto.workflow;

import com.example.mall.agent.exception.AgentErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * AI 工作台统一响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentWorkflowResult {
    private Boolean success;
    private String requestId;
    private String sessionId;
    private String intent;
    private String agentName;
    private String reply;
    private Object data;
    private Long durationMs;
    private AgentErrorCode errorCode;
    private String message;

    public static AgentWorkflowResult fail(AgentWorkflowContext context,
                                           String agentName,
                                           AgentErrorCode errorCode,
                                           String message,
                                           long durationMs) {
        return AgentWorkflowResult.builder()
                .success(false)
                .requestId(context == null ? null : context.getRequestId())
                .sessionId(context == null ? null : context.getSessionId())
                .intent(context == null || context.getIntent() == null ? null : context.getIntent().name())
                .agentName(agentName)
                .reply(message)
                .durationMs(durationMs)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
