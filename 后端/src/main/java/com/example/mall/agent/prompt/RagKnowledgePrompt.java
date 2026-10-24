package com.example.mall.agent.prompt;

import com.example.mall.agent.exception.AiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * RAG 知识库问答 Prompt 提供器，Prompt 独立存放在 resources/prompt 目录。
 */
@Component
public class RagKnowledgePrompt {
    private static final String PROMPT_PATH = "prompt/rag-knowledge-system.txt";

    public String systemPrompt() {
        try {
            return new ClassPathResource(PROMPT_PATH).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AiException(500, "读取 RAG 知识库 Prompt 失败", e);
        }
    }
}
