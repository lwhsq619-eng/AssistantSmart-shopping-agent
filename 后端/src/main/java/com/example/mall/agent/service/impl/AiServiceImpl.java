package com.example.mall.agent.service.impl;

import com.example.mall.agent.config.DeepSeekProperties;
import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.AiMessage;
import com.example.mall.agent.prompt.SystemPromptProvider;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.utils.DeepSeekClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DeepSeek 服务实现，负责选择模型、组装 Prompt 并调用 DeepSeek Client。
 */
@Service
public class AiServiceImpl implements AiService, DeepSeekService {
    private final DeepSeekClient deepSeekClient;
    private final DeepSeekProperties properties;
    private final SystemPromptProvider promptProvider;

    public AiServiceImpl(DeepSeekClient deepSeekClient, DeepSeekProperties properties, SystemPromptProvider promptProvider) {
        this.deepSeekClient = deepSeekClient;
        this.properties = properties;
        this.promptProvider = promptProvider;
    }

    @Override
    public AiChatResponse chat(String message) {
        return deepSeekClient.chat(properties.getChatModel(), buildMessages(message));
    }

    @Override
    public AiChatResponse reason(String message) {
        return deepSeekClient.chat(properties.getReasonerModel(), buildMessages(message));
    }

    @Override
    public AiChatResponse chat(String systemPrompt, String message) {
        return deepSeekClient.chat(properties.getChatModel(), buildMessages(systemPrompt, message));
    }

    @Override
    public AiChatResponse reason(String systemPrompt, String message) {
        return deepSeekClient.chat(properties.getReasonerModel(), buildMessages(systemPrompt, message));
    }

    private List<AiMessage> buildMessages(String message) {
        return buildMessages(promptProvider.generalAssistantPrompt(), message);
    }

    private List<AiMessage> buildMessages(String systemPrompt, String message) {
        return List.of(
                new AiMessage("system", systemPrompt),
                new AiMessage("user", message)
        );
    }
}
