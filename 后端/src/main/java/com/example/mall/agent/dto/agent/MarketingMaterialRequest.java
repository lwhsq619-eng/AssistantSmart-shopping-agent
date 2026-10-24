package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.util.List;

/**
 * 营销素材批量生成请求 DTO。
 */
@Data
public class MarketingMaterialRequest {
    private List<Long> productIds;
    private String activityName;
    private String targetAudience;
    private String style;
    private List<String> channels;
    private String extraRequirement;
}
