package com.example.mall.agent.controller;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.workflow.AgentWorkbenchRequest;
import com.example.mall.agent.dto.workflow.AgentWorkflowResult;
import com.example.mall.agent.exception.AgentErrorCode;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.workflow.MallAgentWorkflow;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 统一 AI 工作台接口。
 * 业务问题进入多 Agent 编排，普通问候/学习/写作/代码问题直接进入 DeepSeek 通用聊天。
 */
@RestController
@RequestMapping("/api/ai/workbench")
public class AgentWorkbenchController {
    private final MallAgentWorkflow mallAgentWorkflow;
    private final DeepSeekService deepSeekService;

    public AgentWorkbenchController(MallAgentWorkflow mallAgentWorkflow, DeepSeekService deepSeekService) {
        this.mallAgentWorkflow = mallAgentWorkflow;
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/chat")
    public AgentWorkflowResult chat(@RequestBody AgentWorkbenchRequest request) {
        if (isGeneralQuestion(request)) {
            return generalChat(request, null);
        }
        AgentWorkflowResult result = mallAgentWorkflow.execute(request);
        if (result != null && result.getErrorCode() == AgentErrorCode.INTENT_UNKNOWN) {
            return generalChat(request, result);
        }
        return result;
    }

    private AgentWorkflowResult generalChat(AgentWorkbenchRequest request, AgentWorkflowResult previousResult) {
        long start = System.currentTimeMillis();
        AiChatResponse response = deepSeekService.chat(request.getMessage());
        long duration = System.currentTimeMillis() - start;
        boolean success = Boolean.TRUE.equals(response.getSuccess());
        String reply = success ? response.getContent() : response.getMessage();
        return AgentWorkflowResult.builder()
                .success(success)
                .requestId(previousResult == null ? UUID.randomUUID().toString() : previousResult.getRequestId())
                .sessionId(previousResult == null ? request.getSessionId() : previousResult.getSessionId())
                .intent("GENERAL_CHAT")
                .agentName("DeepSeekGeneralChat")
                .reply(reply)
                .data(response)
                .durationMs(duration)
                .message(success ? "DeepSeek general chat completed" : reply)
                .build();
    }

    private boolean isGeneralQuestion(AgentWorkbenchRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return false;
        }
        String text = request.getMessage().trim().toLowerCase();
        if (containsAny(text,
                "商品", "订单", "退款", "退货", "换货", "物流", "发货", "库存",
                "优惠券", "购物车", "推荐", "搜索", "购买", "下单", "售后", "评价", "评论",
                "文案", "营销", "简报", "补货", "搭配", "礼品卡")) {
            return false;
        }
        return containsAny(text,
                "你好", "您好", "哈喽", "哈啰", "在吗", "嗨", "hello", "hi", "hey",
                "你是谁", "你能做什么", "介绍一下你", "随便聊", "聊天",
                "什么是", "是什么", "解释", "介绍", "总结", "翻译", "怎么", "为什么",
                "代码", "写一段", "帮我写", "请用", "学习", "区别", "原理",
                "java", "spring", "spring boot", "vue", "mysql", "python", "html", "css", "javascript",
                "算法", "数据库", "操作系统", "计算机网络", "编译原理");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
