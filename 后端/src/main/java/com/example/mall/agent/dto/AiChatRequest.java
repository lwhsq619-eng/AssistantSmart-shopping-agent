package com.example.mall.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 聊天请求参数。
 */
@Data
public class AiChatRequest {
    @NotBlank(message = "消息内容不能为空")
    private String message;
}
