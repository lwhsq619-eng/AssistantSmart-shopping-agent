package com.example.mall.agent.service;

import com.example.mall.agent.dto.AiChatResponse;

/**
 * AI 服务接口。
 * Controller 必须通过 Service 调用 DeepSeek，不能直接写 HTTP 调用逻辑。
 */
public interface AiService {
    AiChatResponse chat(String message);

    AiChatResponse reason(String message);

    AiChatResponse chat(String systemPrompt, String message);

    AiChatResponse reason(String systemPrompt, String message);
}
