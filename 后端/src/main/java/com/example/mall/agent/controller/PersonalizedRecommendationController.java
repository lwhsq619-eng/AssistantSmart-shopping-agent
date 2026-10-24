package com.example.mall.agent.controller;

import com.example.mall.agent.agent.PersonalizedRecommendationAgent;
import com.example.mall.agent.dto.agent.PersonalizedRecommendationRequest;
import com.example.mall.agent.dto.agent.PersonalizedRecommendationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个性化商品推荐接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/recommend")
public class PersonalizedRecommendationController {
    private final PersonalizedRecommendationAgent recommendationAgent;

    public PersonalizedRecommendationController(PersonalizedRecommendationAgent recommendationAgent) {
        this.recommendationAgent = recommendationAgent;
    }

    @PostMapping("/personalized")
    public PersonalizedRecommendationResponse personalized(@RequestBody PersonalizedRecommendationRequest request) {
        return recommendationAgent.recommend(request);
    }
}
