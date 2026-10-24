package com.example.mall.agent.controller;

import com.example.mall.agent.monitor.AgentHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI Agent 监控接口。
 */
@RestController
@RequestMapping("/api/ai")
public class AgentMonitorController {
    private final AgentHealthService agentHealthService;

    public AgentMonitorController(AgentHealthService agentHealthService) {
        this.agentHealthService = agentHealthService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return agentHealthService.health();
    }
}
