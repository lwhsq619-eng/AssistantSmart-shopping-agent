package com.example.mall.agent.router;

import org.springframework.stereotype.Component;

/**
 * 统一 Agent Router。
 */
@Component
public class AgentRouter {
    private final AgentIntentClassifier intentClassifier;

    public AgentRouter(AgentIntentClassifier intentClassifier) {
        this.intentClassifier = intentClassifier;
    }

    public AgentIntent route(String message) {
        return intentClassifier.classify(message);
    }

    public String agentName(AgentIntent intent) {
        return switch (intent) {
            case SHOPPING_GUIDE -> "ShoppingGuideAgent";
            case CUSTOMER_SERVICE -> "CustomerServiceAgent";
            case SEMANTIC_SEARCH -> "SemanticSearchAgent";
            case RETENTION -> "RetentionAgent";
            case COPYWRITING -> "MerchantCopywritingAgent";
            case COMMENT_MANAGEMENT -> "CommentManagementAgent";
            case MARKETING -> "MarketingMaterialAgent";
            case ORDER_ANALYSIS -> "OrderDataAnalysisAgent";
            case AFTER_SALE -> "AfterSaleTicketAgent";
            case INVENTORY_ANALYSIS -> "InventorySalesAnalysisAgent";
            case OPERATION_BRIEFING -> "OperationBriefingAgent";
            case PERSONALIZED_RECOMMENDATION -> "PersonalizedRecommendationAgent";
            case SCENE_MATCH -> "SceneMatchAgent";
            case CONTENT_SAFETY -> "ContentSafetyAgent";
            case RAG_KNOWLEDGE -> "RagKnowledgeAgent";
            case UNKNOWN -> "UnknownAgent";
        };
    }
}
