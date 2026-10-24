package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 个性化推荐响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalizedRecommendationResponse {
    private Boolean success;
    private Long userId;
    private String scene;
    private String groupTitle;
    private String preferenceSummary;
    private List<String> preferenceTags;
    private List<RecommendationProductItem> products;
    private Boolean fallbackUsed;
    private String message;

    public static PersonalizedRecommendationResponse fail(Long userId, String scene, String message) {
        return PersonalizedRecommendationResponse.builder()
                .success(false)
                .userId(userId)
                .scene(scene)
                .products(List.of())
                .preferenceTags(List.of())
                .message(message)
                .build();
    }
}
