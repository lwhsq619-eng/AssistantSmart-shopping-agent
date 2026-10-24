package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 内容安全单次审核结果，可表示 Java 初筛或 AI 精审结果。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentSafetyResult {
    private Boolean safe;
    private String riskLevel;
    private List<String> categories;
    private String suggestion;
    private String reason;
    private String source;
}
