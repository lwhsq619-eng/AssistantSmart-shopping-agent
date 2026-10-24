package com.example.mall.agent.controller;

import com.example.mall.agent.dto.AiChatRequest;
import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.agent.service.DeepSeekService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 聊天控制器。
 * 只负责接收请求和返回结果，不直接编写 DeepSeek HTTP 调用逻辑。
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final DeepSeekService deepSeekService;

    public AiController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody AiChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return AiChatResponse.fail("message 不能为空");
        }
        try {
            return deepSeekService.chat(request.getMessage());
        } catch (AiException e) {
            return AiChatResponse.fail(e.getMessage());
        } catch (Exception e) {
            return AiChatResponse.fail("AI 服务异常，请稍后重试");
        }
    }
}
