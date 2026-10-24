package com.example.mall.agent.controller;

import com.example.mall.agent.agent.SceneMatchAgent;
import com.example.mall.agent.dto.agent.SceneMatchRequest;
import com.example.mall.agent.dto.agent.SceneMatchResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 场景化搭配推荐接口。
 * Controller 只调用 Agent，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/match")
public class SceneMatchController {
    private final SceneMatchAgent sceneMatchAgent;

    public SceneMatchController(SceneMatchAgent sceneMatchAgent) {
        this.sceneMatchAgent = sceneMatchAgent;
    }

    @PostMapping("/scene")
    public SceneMatchResponse sceneMatch(@RequestBody SceneMatchRequest request) {
        return sceneMatchAgent.match(request);
    }
}
