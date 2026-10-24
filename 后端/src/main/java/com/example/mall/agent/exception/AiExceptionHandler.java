package com.example.mall.agent.exception;

import com.example.mall.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * AI 模块异常处理器。
 * 第一阶段 /api/ai/chat 已在 Controller 内返回 AiChatResponse，本处理器用于兜底处理未捕获异常。
 */
@RestControllerAdvice(basePackages = "com.example.mall.agent")
public class AiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AiExceptionHandler.class);

    @ExceptionHandler(AiException.class)
    public Result<Void> handleAiException(AiException e) {
        log.error("AI module exception, code={}, message={}", e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }
}
