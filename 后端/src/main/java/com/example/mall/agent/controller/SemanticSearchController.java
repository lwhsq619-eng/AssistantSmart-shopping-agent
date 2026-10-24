package com.example.mall.agent.controller;

import com.example.mall.agent.agent.SemanticSearchAgent;
import com.example.mall.agent.dto.agent.SemanticSearchRequest;
import com.example.mall.agent.dto.agent.SemanticSearchResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语义化商品搜索接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek 或 Tool。
 */
@RestController
@RequestMapping("/api/ai/search")
public class SemanticSearchController {
    private final SemanticSearchAgent semanticSearchAgent;

    public SemanticSearchController(SemanticSearchAgent semanticSearchAgent) {
        this.semanticSearchAgent = semanticSearchAgent;
    }

    @PostMapping("/semantic")
    public SemanticSearchResponse semanticSearch(@Valid @RequestBody SemanticSearchRequest request) {
        return semanticSearchAgent.search(request);
    }
}
