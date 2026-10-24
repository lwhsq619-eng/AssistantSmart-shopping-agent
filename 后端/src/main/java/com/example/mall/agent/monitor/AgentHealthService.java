package com.example.mall.agent.monitor;

import com.example.mall.agent.config.DeepSeekProperties;
import com.example.mall.agent.rag.RagVectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Agent 健康检查服务。
 */
@Service
public class AgentHealthService {
    private static final List<String> ENABLED_AGENTS = List.of(
            "ShoppingGuideAgent",
            "CustomerServiceAgent",
            "SemanticSearchAgent",
            "RetentionAgent",
            "MerchantCopywritingAgent",
            "CommentManagementAgent",
            "MarketingMaterialAgent",
            "OrderDataAnalysisAgent",
            "AfterSaleTicketAgent",
            "InventorySalesAnalysisAgent",
            "OperationBriefingAgent",
            "PersonalizedRecommendationAgent",
            "SceneMatchAgent",
            "ContentSafetyAgent",
            "RagKnowledgeAgent"
    );
    private static final List<String> PROMPT_FILES = List.of(
            "prompt/shopping-guide-system.txt",
            "prompt/customer-service-system.txt",
            "prompt/semantic-search-system.txt",
            "prompt/retention-system.txt",
            "prompt/merchant-copywriting-system.txt",
            "prompt/comment-management-system.txt",
            "prompt/marketing-material-system.txt",
            "prompt/order-data-analysis-system.txt",
            "prompt/after-sale-ticket-system.txt",
            "prompt/inventory-sales-analysis-system.txt",
            "prompt/operation-briefing-system.txt",
            "prompt/personalized-recommendation-system.txt",
            "prompt/scene-match-system.txt",
            "prompt/content-safety-system.txt",
            "prompt/rag-knowledge-system.txt"
    );
    private static final List<String> TOOL_BEANS = List.of(
            "productTool",
            "orderTool",
            "userTool",
            "commentTool",
            "inventoryTool"
    );

    private final DeepSeekProperties deepSeekProperties;
    private final RagVectorStore ragVectorStore;
    private final ApplicationContext applicationContext;

    public AgentHealthService(DeepSeekProperties deepSeekProperties,
                              RagVectorStore ragVectorStore,
                              ApplicationContext applicationContext) {
        this.deepSeekProperties = deepSeekProperties;
        this.ragVectorStore = ragVectorStore;
        this.applicationContext = applicationContext;
    }

    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("deepseekAvailable", StringUtils.hasText(deepSeekProperties.getApiKey()));
        result.put("enabledAgents", ENABLED_AGENTS);
        result.put("promptFiles", checkPromptFiles());
        result.put("ragDocumentCount", ragVectorStore.allChunks().stream()
                .map(chunk -> chunk.getDocumentId())
                .distinct()
                .count());
        result.put("ragChunkCount", ragVectorStore.allChunks().size());
        result.put("toolStatus", checkToolStatus());
        return result;
    }

    private Map<String, Boolean> checkPromptFiles() {
        Map<String, Boolean> status = new LinkedHashMap<>();
        for (String promptFile : PROMPT_FILES) {
            status.put(promptFile, new ClassPathResource(promptFile).exists());
        }
        return status;
    }

    private Map<String, Boolean> checkToolStatus() {
        Map<String, Boolean> status = new LinkedHashMap<>();
        for (String toolBean : TOOL_BEANS) {
            status.put(toolBean, applicationContext.containsBean(toolBean));
        }
        return status;
    }
}
