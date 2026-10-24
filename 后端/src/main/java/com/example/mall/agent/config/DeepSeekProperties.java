package com.example.mall.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek API 配置。
 * API Key 只允许从环境变量或外部配置读取，禁止写死在代码中。
 */
@Data
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {
    private String baseUrl = "https://api.deepseek.com";
    private String apiKey;
    private String chatModel = "deepseek-chat";
    private String reasonerModel = "deepseek-reasoner";
    private Integer timeoutSeconds = 60;
    private Integer maxRetries = 2;
}
