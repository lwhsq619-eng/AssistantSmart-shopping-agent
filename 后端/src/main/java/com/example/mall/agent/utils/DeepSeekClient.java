package com.example.mall.agent.utils;

import com.example.mall.agent.config.DeepSeekProperties;
import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.AiMessage;
import com.example.mall.agent.dto.DeepSeekChatRequest;
import com.example.mall.agent.dto.DeepSeekChatResponse;
import com.example.mall.agent.exception.AiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * DeepSeek OpenAI Compatible 最小客户端。
 * 第一阶段只实现非流式聊天，不打印 API Key。
 */
public class DeepSeekClient {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final DeepSeekProperties properties;

    public DeepSeekClient(HttpClient httpClient, ObjectMapper objectMapper, DeepSeekProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 调用 DeepSeek /chat/completions 接口。
     */
    public AiChatResponse chat(String model, List<AiMessage> messages) {
        validateApiKey();
        long start = System.currentTimeMillis();
        int attempt = 0;
        Exception lastError = null;

        while (attempt <= properties.getMaxRetries()) {
            try {
                DeepSeekChatRequest deepSeekRequest = new DeepSeekChatRequest(model, messages, false);
                String body = objectMapper.writeValueAsString(deepSeekRequest);
                HttpRequest request = baseRequest()
                        .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                long elapsed = System.currentTimeMillis() - start;
                handleHttpStatus(response.statusCode(), response.body(), model, elapsed);

                DeepSeekChatResponse deepSeekResponse = parseResponse(response.body());
                String content = extractContent(deepSeekResponse);
                Integer totalTokens = deepSeekResponse.getUsage() == null ? null : deepSeekResponse.getUsage().getTotalTokens();
                log.info("DeepSeek chat success, model={}, elapsedMs={}, tokens={}", model, elapsed, totalTokens);
                return AiChatResponse.ok(model, content);
            } catch (AiException e) {
                log.error("DeepSeek chat failed, model={}, attempt={}, code={}, message={}", model, attempt, e.getCode(), e.getMessage());
                throw e;
            } catch (Exception e) {
                lastError = e;
                attempt++;
                log.warn("DeepSeek chat retry, model={}, attempt={}, error={}", model, attempt, e.getMessage());
                if (attempt > properties.getMaxRetries()) {
                    break;
                }
                sleepBeforeRetry(attempt);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.error("DeepSeek chat final failure, model={}, elapsedMs={}", model, elapsed, lastError);
        if (lastError instanceof java.net.http.HttpTimeoutException || lastError instanceof SocketTimeoutException) {
            throw new AiException(504, "DeepSeek API 请求超时", lastError);
        }
        throw new AiException(500, "DeepSeek API 调用失败", lastError);
    }

    private HttpRequest.Builder baseRequest() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json");
    }

    private void validateApiKey() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiException(500, "未配置环境变量 DEEPSEEK_API_KEY");
        }
    }

    private void handleHttpStatus(int statusCode, String body, String model, long elapsed) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        log.error("DeepSeek HTTP error, model={}, status={}, elapsedMs={}", model, statusCode, elapsed);
        if (statusCode == 401) {
            throw new AiException(401, "DeepSeek API Key 无效或未授权");
        }
        if (statusCode == 429) {
            throw new AiException(429, "DeepSeek API 请求过于频繁，请稍后重试");
        }
        throw new AiException(statusCode, "DeepSeek API 返回异常状态码：" + statusCode + "，响应：" + body);
    }

    private DeepSeekChatResponse parseResponse(String body) {
        try {
            return objectMapper.readValue(body, DeepSeekChatResponse.class);
        } catch (JsonProcessingException e) {
            throw new AiException(500, "DeepSeek 返回体解析失败", e);
        }
    }

    private String extractContent(DeepSeekChatResponse response) {
        if (response.getChoices() == null || response.getChoices().isEmpty()
                || response.getChoices().get(0).getMessage() == null
                || response.getChoices().get(0).getMessage().getContent() == null) {
            throw new AiException(500, "DeepSeek 返回内容为空");
        }
        return response.getChoices().get(0).getMessage().getContent();
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(Math.min(1000L * attempt, 3000L));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
