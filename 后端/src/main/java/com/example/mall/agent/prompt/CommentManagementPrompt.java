package com.example.mall.agent.prompt;

import com.example.mall.agent.exception.AiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 评论智能管理 Prompt 提供器，Prompt 独立存放在 resources/prompt 目录。
 */
@Component
public class CommentManagementPrompt {
    private static final String PROMPT_PATH = "prompt/comment-management-system.txt";

    public String systemPrompt() {
        try {
            return new ClassPathResource(PROMPT_PATH).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AiException(500, "读取评论管理 Prompt 失败", e);
        }
    }
}
