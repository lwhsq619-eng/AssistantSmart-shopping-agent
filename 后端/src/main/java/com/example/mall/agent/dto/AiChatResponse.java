package com.example.mall.agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天接口返回对象，严格匹配第一阶段返回格式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiChatResponse {
    private Boolean success;
    private String model;
    private String content;
    private String message;

    public static AiChatResponse ok(String model, String content) {
        return new AiChatResponse(true, model, content, null);
    }

    public static AiChatResponse fail(String message) {
        return new AiChatResponse(false, null, null, message);
    }
}
