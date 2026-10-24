package com.example.mall.agent.controller;

import com.example.mall.agent.agent.RetentionAgent;
import com.example.mall.agent.dto.agent.RetentionRequest;
import com.example.mall.agent.dto.agent.RetentionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流失用户挽留接口。
 * Controller 只调用 RetentionAgent，不直接调用 Tool 或 DeepSeekService。
 */
@RestController
@RequestMapping("/api/ai/retention")
public class RetentionController {
    private final RetentionAgent retentionAgent;

    public RetentionController(RetentionAgent retentionAgent) {
        this.retentionAgent = retentionAgent;
    }

    @PostMapping("/generate")
    public RetentionResponse generate(@Valid @RequestBody RetentionRequest request) {
        return retentionAgent.generate(request);
    }
}
