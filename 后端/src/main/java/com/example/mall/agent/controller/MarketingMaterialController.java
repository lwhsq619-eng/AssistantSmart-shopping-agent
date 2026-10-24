package com.example.mall.agent.controller;

import com.example.mall.agent.agent.MarketingMaterialAgent;
import com.example.mall.agent.dto.agent.MarketingMaterialRequest;
import com.example.mall.agent.dto.agent.MarketingMaterialResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家运营营销素材生成接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek，不发布营销内容。
 */
@RestController
@RequestMapping("/api/ai/merchant/marketing")
public class MarketingMaterialController {
    private final MarketingMaterialAgent marketingMaterialAgent;

    public MarketingMaterialController(MarketingMaterialAgent marketingMaterialAgent) {
        this.marketingMaterialAgent = marketingMaterialAgent;
    }

    @PostMapping("/generate")
    public MarketingMaterialResponse generate(@RequestBody MarketingMaterialRequest request) {
        return marketingMaterialAgent.generate(request);
    }
}
