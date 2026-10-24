package com.example.mall.agent.exception;

/**
 * AI Agent 统一错误码。
 */
public enum AgentErrorCode {
    AI_API_ERROR,
    AI_TIMEOUT,
    PROMPT_NOT_FOUND,
    TOOL_EXECUTION_ERROR,
    INTENT_UNKNOWN,
    PERMISSION_DENIED,
    SAFETY_BLOCKED,
    RAG_NO_EVIDENCE,
    PARAM_ERROR
}
