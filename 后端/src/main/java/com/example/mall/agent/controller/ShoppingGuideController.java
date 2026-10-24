package com.example.mall.agent.controller;

import com.example.mall.agent.agent.ShoppingGuideAgent;
import com.example.mall.agent.dto.agent.ShoppingGuideRequest;
import com.example.mall.agent.dto.agent.ShoppingGuideResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能导购接口 Controller。
 * Controller 只接收请求并调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/guide")
public class ShoppingGuideController {
    private final ShoppingGuideAgent shoppingGuideAgent;

    public ShoppingGuideController(ShoppingGuideAgent shoppingGuideAgent) {
        this.shoppingGuideAgent = shoppingGuideAgent;
    }

    @PostMapping("/chat")
    public ShoppingGuideResponse chat(@RequestBody ShoppingGuideRequest request) {
        return shoppingGuideAgent.chat(request);
    }
}
