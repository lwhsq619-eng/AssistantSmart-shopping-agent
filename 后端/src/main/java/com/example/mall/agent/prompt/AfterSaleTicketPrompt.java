package com.example.mall.agent.prompt;

import com.example.mall.agent.exception.AiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 售后工单智能分拣 Prompt 提供器，Prompt 独立存放在 resources/prompt 目录。
 */
@Component
public class AfterSaleTicketPrompt {
    private static final String PROMPT_PATH = "prompt/after-sale-ticket-system.txt";

    public String systemPrompt() {
        try {
            return new ClassPathResource(PROMPT_PATH).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AiException(500, "读取售后工单 Prompt 失败", e);
        }
    }
}
