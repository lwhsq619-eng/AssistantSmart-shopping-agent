package com.example.mall.agent.controller;

import com.example.mall.agent.agent.CommentManagementAgent;
import com.example.mall.agent.dto.agent.CommentAnalysisRequest;
import com.example.mall.agent.dto.agent.CommentAnalysisResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家运营评论智能管理接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek，不发布评论回复。
 */
@RestController
@RequestMapping("/api/ai/merchant/comment")
public class CommentManagementController {
    private final CommentManagementAgent commentManagementAgent;

    public CommentManagementController(CommentManagementAgent commentManagementAgent) {
        this.commentManagementAgent = commentManagementAgent;
    }

    @PostMapping("/analyze")
    public CommentAnalysisResponse analyze(@RequestBody CommentAnalysisRequest request) {
        return commentManagementAgent.analyze(request);
    }
}
