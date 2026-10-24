package com.example.mall.agent.controller;

import com.example.mall.agent.agent.ContentSafetyAgent;
import com.example.mall.agent.dto.agent.ContentSafetyRequest;
import com.example.mall.agent.dto.agent.ContentSafetyResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容安全审核接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/safety")
public class ContentSafetyController {
    private final ContentSafetyAgent contentSafetyAgent;

    public ContentSafetyController(ContentSafetyAgent contentSafetyAgent) {
        this.contentSafetyAgent = contentSafetyAgent;
    }

    @PostMapping("/review")
    public ContentSafetyResponse review(@RequestBody ContentSafetyRequest request) {
        return contentSafetyAgent.review(request);
    }
}
