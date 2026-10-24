package com.example.mall.agent.controller;

import com.example.mall.agent.agent.OperationBriefingAgent;
import com.example.mall.agent.dto.agent.OperationBriefingRequest;
import com.example.mall.agent.dto.agent.OperationBriefingResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自动化运营简报接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/operation")
public class OperationBriefingController {
    private final OperationBriefingAgent operationBriefingAgent;

    public OperationBriefingController(OperationBriefingAgent operationBriefingAgent) {
        this.operationBriefingAgent = operationBriefingAgent;
    }

    @PostMapping("/briefing")
    public OperationBriefingResponse briefing(@RequestBody OperationBriefingRequest request) {
        return operationBriefingAgent.generate(request);
    }
}
