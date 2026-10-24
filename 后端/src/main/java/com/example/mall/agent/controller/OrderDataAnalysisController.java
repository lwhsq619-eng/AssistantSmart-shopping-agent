package com.example.mall.agent.controller;

import com.example.mall.agent.agent.OrderDataAnalysisAgent;
import com.example.mall.agent.dto.agent.OrderDataAnalysisRequest;
import com.example.mall.agent.dto.agent.OrderDataAnalysisResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家运营订单数据分析接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/merchant")
public class OrderDataAnalysisController {
    private final OrderDataAnalysisAgent orderDataAnalysisAgent;

    public OrderDataAnalysisController(OrderDataAnalysisAgent orderDataAnalysisAgent) {
        this.orderDataAnalysisAgent = orderDataAnalysisAgent;
    }

    @PostMapping("/order-analysis")
    public OrderDataAnalysisResponse analyze(@RequestBody OrderDataAnalysisRequest request) {
        return orderDataAnalysisAgent.analyze(request);
    }
}
