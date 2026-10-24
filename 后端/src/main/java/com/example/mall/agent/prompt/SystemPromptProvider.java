package com.example.mall.agent.prompt;

import org.springframework.stereotype.Component;

/**
 * AI Prompt 统一管理类，避免 Controller 硬编码 Prompt。
 */
@Component
public class SystemPromptProvider {
    public String generalAssistantPrompt() {
        return "你是网页端通用 AI 智能助手，已接入 DeepSeek。"
                + "你可以回答电商系统、Java、学习、写作、代码、解释、翻译、总结等通用问题。"
                + "回答要清晰、准确、简洁。"
                + "涉及商城商品、订单、库存、优惠等业务数据时，不能编造数据库不存在的信息。";
    }
}
