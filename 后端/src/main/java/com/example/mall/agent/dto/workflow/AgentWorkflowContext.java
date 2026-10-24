package com.example.mall.agent.dto.workflow;

import com.example.mall.agent.router.AgentIntent;
import lombok.Builder;
import lombok.Data;

/**
 * Agent 工作流上下文。
 */
@Data
@Builder
public class AgentWorkflowContext {
    private String requestId;
    private Long userId;
    private String role;
    private String sessionId;
    private String message;
    private AgentIntent intent;
}
