package com.example.mall.agent.router;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Agent 意图分类器。
 * 第一层使用 Java 关键词规则；规则无法判断时调用 deepseek-chat 做轻量识别。
 */
@Component
public class AgentIntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(AgentIntentClassifier.class);

    private final AiService aiService;

    public AgentIntentClassifier(AiService aiService) {
        this.aiService = aiService;
    }

    public AgentIntent classify(String message) {
        AgentIntent ruleIntent = classifyByRule(message);
        if (ruleIntent != AgentIntent.UNKNOWN) {
            return ruleIntent;
        }
        try {
            String systemPrompt = "你是电商 AI Agent 意图分类器，只能返回一个枚举值。";
            String prompt = "请判断用户输入应路由到哪个 Agent，只返回枚举名："
                    + "SHOPPING_GUIDE,CUSTOMER_SERVICE,SEMANTIC_SEARCH,RETENTION,COPYWRITING,COMMENT_MANAGEMENT,"
                    + "MARKETING,ORDER_ANALYSIS,AFTER_SALE,INVENTORY_ANALYSIS,OPERATION_BRIEFING,"
                    + "PERSONALIZED_RECOMMENDATION,SCENE_MATCH,CONTENT_SAFETY,RAG_KNOWLEDGE,UNKNOWN。\n"
                    + "用户输入：" + message;
            AiChatResponse response = aiService.chat(systemPrompt, prompt);
            if (Boolean.TRUE.equals(response.getSuccess()) && StringUtils.hasText(response.getContent())) {
                return parseIntent(response.getContent());
            }
        } catch (Exception e) {
            log.warn("AgentIntentClassifier AI fallback failed, error={}", e.getMessage());
        }
        return AgentIntent.UNKNOWN;
    }

    private AgentIntent classifyByRule(String message) {
        if (!StringUtils.hasText(message)) {
            return AgentIntent.UNKNOWN;
        }
        String text = message.toLowerCase(Locale.ROOT);
        if (contains(text, "安全", "审核", "违规", "拦截", "封禁")) {
            return AgentIntent.CONTENT_SAFETY;
        }
        if (contains(text, "知识库", "规则", "政策", "退换货", "七天无理由", "平台帮助")) {
            return AgentIntent.RAG_KNOWLEDGE;
        }
        if (contains(text, "搭配", "穿搭", "宿舍", "组合", "成套", "礼物")) {
            return AgentIntent.SCENE_MATCH;
        }
        if (contains(text, "个性化", "猜你喜欢", "为我推荐", "我的偏好")) {
            return AgentIntent.PERSONALIZED_RECOMMENDATION;
        }
        if (contains(text, "搜索", "找一下", "有没有", "查找")) {
            return AgentIntent.SEMANTIC_SEARCH;
        }
        if (contains(text, "订单", "发货", "退款", "售后", "退货", "换货", "物流")) {
            return AgentIntent.CUSTOMER_SERVICE;
        }
        if (contains(text, "推荐", "买", "适合", "预算", "以内")) {
            return AgentIntent.SHOPPING_GUIDE;
        }
        if (contains(text, "文案", "标题", "卖点")) {
            return AgentIntent.COPYWRITING;
        }
        if (contains(text, "评论", "差评", "回复")) {
            return AgentIntent.COMMENT_MANAGEMENT;
        }
        if (contains(text, "营销", "活动", "直播", "短视频", "海报", "推送")) {
            return AgentIntent.MARKETING;
        }
        if (contains(text, "营收", "订单分析", "客单价", "复购", "消费分析")) {
            return AgentIntent.ORDER_ANALYSIS;
        }
        if (contains(text, "补货", "库存", "滞销", "缺货", "清仓")) {
            return AgentIntent.INVENTORY_ANALYSIS;
        }
        if (contains(text, "简报", "日报", "周报", "月报")) {
            return AgentIntent.OPERATION_BRIEFING;
        }
        if (contains(text, "挽留", "加购未付款", "退出挽留")) {
            return AgentIntent.RETENTION;
        }
        return AgentIntent.UNKNOWN;
    }

    private boolean contains(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private AgentIntent parseIntent(String content) {
        String upper = content.trim().toUpperCase(Locale.ROOT);
        for (AgentIntent intent : AgentIntent.values()) {
            if (upper.contains(intent.name())) {
                return intent;
            }
        }
        return AgentIntent.UNKNOWN;
    }
}
