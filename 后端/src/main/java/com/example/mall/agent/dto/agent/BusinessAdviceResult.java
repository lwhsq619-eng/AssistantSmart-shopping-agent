package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 经营建议结果 DTO。
 */
@Data
@Builder
public class BusinessAdviceResult {
    private List<String> selectionAdvice;
    private List<String> pricingAdvice;
    private List<String> activityAdvice;
    private List<String> riskWarnings;
}
