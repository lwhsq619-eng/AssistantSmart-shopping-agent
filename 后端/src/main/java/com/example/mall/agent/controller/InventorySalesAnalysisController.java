package com.example.mall.agent.controller;

import com.example.mall.agent.agent.InventorySalesAnalysisAgent;
import com.example.mall.agent.dto.agent.InventorySalesAnalysisRequest;
import com.example.mall.agent.dto.agent.InventorySalesAnalysisResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存销售分析接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/inventory")
public class InventorySalesAnalysisController {
    private final InventorySalesAnalysisAgent inventorySalesAnalysisAgent;

    public InventorySalesAnalysisController(InventorySalesAnalysisAgent inventorySalesAnalysisAgent) {
        this.inventorySalesAnalysisAgent = inventorySalesAnalysisAgent;
    }

    @PostMapping("/analysis")
    public InventorySalesAnalysisResponse analyze(@RequestBody InventorySalesAnalysisRequest request) {
        return inventorySalesAnalysisAgent.analyze(request);
    }
}
