package com.example.mall.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek /chat/completions 请求体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatRequest {
    private String model;
    private List<AiMessage> messages;
    private Boolean stream;
}
