package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 挽留策略结果 DTO，对应 DeepSeek JSON 输出结构。
 */
@Data
@Builder
public class RetentionStrategyResult {
    private String strategyType;
    private String message;
    private List<Long> recommendedProductIds;
    private Boolean couponAvailable;
    private Boolean aiGenerated;
}
