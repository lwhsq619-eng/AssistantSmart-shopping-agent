package com.example.mall.agent.dto;

/**
 * AI Token 使用统计。
 */
public record AiUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
}
