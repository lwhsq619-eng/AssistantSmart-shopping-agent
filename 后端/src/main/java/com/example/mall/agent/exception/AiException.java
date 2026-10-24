package com.example.mall.agent.exception;

import lombok.Getter;

/**
 * AI 模块统一异常，保留错误码便于前端展示。
 */
@Getter
public class AiException extends RuntimeException {
    private final Integer code;

    public AiException(String message) {
        super(message);
        this.code = 500;
    }

    public AiException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public AiException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
