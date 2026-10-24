package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 营销素材批量生成响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketingMaterialResponse {
    private Boolean success;
    private String activityName;
    private List<MarketingMaterialItem> materials;
    private String message;

    public static MarketingMaterialResponse fail(String activityName, String message) {
        return MarketingMaterialResponse.builder()
                .success(false)
                .activityName(activityName)
                .materials(List.of())
                .message(message)
                .build();
    }
}
