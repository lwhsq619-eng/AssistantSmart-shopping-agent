package com.example.mall.agent.config;

import com.example.mall.agent.utils.DeepSeekClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI 客户端配置类，创建 DeepSeek OpenAI Compatible Client。
 */
@Configuration
@EnableConfigurationProperties(DeepSeekProperties.class)
public class AiConfig {
    @Bean
    public HttpClient aiHttpClient(DeepSeekProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Bean
    public DeepSeekClient deepSeekClient(HttpClient aiHttpClient, ObjectMapper objectMapper, DeepSeekProperties properties) {
        return new DeepSeekClient(aiHttpClient, objectMapper, properties);
    }
}
