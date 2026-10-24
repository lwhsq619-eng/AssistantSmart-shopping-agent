package com.example.mall.agent.prompt;

import com.example.mall.agent.exception.AiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 智能导购 Prompt 提供器，Prompt 文本独立存放在 resources/prompt 目录。
 */
@Component
public class ShoppingGuidePrompt {
    private static final String PROMPT_PATH = "prompt/shopping-guide-system.txt";

    public String systemPrompt() {
        try {
            return new ClassPathResource(PROMPT_PATH).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AiException(500, "读取智能导购 Prompt 失败", e);
        }
    }
}
