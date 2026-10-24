package com.assistantsmart.shopping.controller;

import com.assistantsmart.shopping.model.ChatRequest;
import com.assistantsmart.shopping.model.ChatResponse;
import com.assistantsmart.shopping.service.LangGraphShoppingOrchestrator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopping")
public class ShoppingGuideController {

    private final LangGraphShoppingOrchestrator orchestrator;

    public ShoppingGuideController(LangGraphShoppingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/guide")
    public ChatResponse guide(@Valid @RequestBody ChatRequest request) {
        return orchestrator.guide(request.sessionId(), request.message());
    }
}
