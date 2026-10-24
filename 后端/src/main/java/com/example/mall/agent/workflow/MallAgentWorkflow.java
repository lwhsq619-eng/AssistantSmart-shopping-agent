package com.example.mall.agent.workflow;

import com.example.mall.agent.agent.AfterSaleTicketAgent;
import com.example.mall.agent.agent.CommentManagementAgent;
import com.example.mall.agent.agent.ContentSafetyAgent;
import com.example.mall.agent.agent.CustomerServiceAgent;
import com.example.mall.agent.agent.InventorySalesAnalysisAgent;
import com.example.mall.agent.agent.MarketingMaterialAgent;
import com.example.mall.agent.agent.MerchantCopywritingAgent;
import com.example.mall.agent.agent.OperationBriefingAgent;
import com.example.mall.agent.agent.OrderDataAnalysisAgent;
import com.example.mall.agent.agent.PersonalizedRecommendationAgent;
import com.example.mall.agent.agent.RagKnowledgeAgent;
import com.example.mall.agent.agent.RetentionAgent;
import com.example.mall.agent.agent.SceneMatchAgent;
import com.example.mall.agent.agent.SemanticSearchAgent;
import com.example.mall.agent.agent.ShoppingGuideAgent;
import com.example.mall.agent.dto.agent.AfterSaleTicketRequest;
import com.example.mall.agent.dto.agent.CommentAnalysisRequest;
import com.example.mall.agent.dto.agent.ContentSafetyRequest;
import com.example.mall.agent.dto.agent.ContentSafetyResponse;
import com.example.mall.agent.dto.agent.CopywritingRequest;
import com.example.mall.agent.dto.agent.CustomerServiceRequest;
import com.example.mall.agent.dto.agent.InventorySalesAnalysisRequest;
import com.example.mall.agent.dto.agent.MarketingMaterialRequest;
import com.example.mall.agent.dto.agent.OperationBriefingRequest;
import com.example.mall.agent.dto.agent.OrderDataAnalysisRequest;
import com.example.mall.agent.dto.agent.PersonalizedRecommendationRequest;
import com.example.mall.agent.dto.agent.RagKnowledgeRequest;
import com.example.mall.agent.dto.agent.RetentionRequest;
import com.example.mall.agent.dto.agent.SceneMatchRequest;
import com.example.mall.agent.dto.agent.SemanticSearchRequest;
import com.example.mall.agent.dto.agent.ShoppingGuideRequest;
import com.example.mall.agent.dto.workflow.AgentWorkbenchRequest;
import com.example.mall.agent.dto.workflow.AgentWorkflowContext;
import com.example.mall.agent.dto.workflow.AgentWorkflowResult;
import com.example.mall.agent.exception.AgentErrorCode;
import com.example.mall.agent.router.AgentIntent;
import com.example.mall.agent.router.AgentRouter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多 Agent 编排工作流。
 * 流程：内容安全审核 -> 意图识别 -> 权限校验 -> 目标 Agent 执行 -> 统一返回。
 */
@Service
public class MallAgentWorkflow {
    private static final Logger log = LoggerFactory.getLogger(MallAgentWorkflow.class);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern BUDGET_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(元|块|以内|以下)?");
    private static final Set<String> ROLES = Set.of("USER", "MERCHANT", "ADMIN", "WAREHOUSE", "CUSTOMER_SERVICE");
    private static final Map<String, Set<AgentIntent>> ROLE_PERMISSIONS = Map.of(
            "USER", Set.of(
                    AgentIntent.SHOPPING_GUIDE,
                    AgentIntent.CUSTOMER_SERVICE,
                    AgentIntent.SEMANTIC_SEARCH,
                    AgentIntent.RETENTION,
                    AgentIntent.PERSONALIZED_RECOMMENDATION,
                    AgentIntent.SCENE_MATCH,
                    AgentIntent.RAG_KNOWLEDGE
            ),
            "MERCHANT", Set.of(
                    AgentIntent.COPYWRITING,
                    AgentIntent.COMMENT_MANAGEMENT,
                    AgentIntent.MARKETING,
                    AgentIntent.ORDER_ANALYSIS,
                    AgentIntent.OPERATION_BRIEFING
            ),
            "WAREHOUSE", Set.of(
                    AgentIntent.AFTER_SALE,
                    AgentIntent.INVENTORY_ANALYSIS,
                    AgentIntent.OPERATION_BRIEFING
            ),
            "CUSTOMER_SERVICE", Set.of(
                    AgentIntent.CUSTOMER_SERVICE,
                    AgentIntent.AFTER_SALE,
                    AgentIntent.RAG_KNOWLEDGE,
                    AgentIntent.CONTENT_SAFETY
            )
    );

    private final AgentRouter agentRouter;
    private final ContentSafetyAgent contentSafetyAgent;
    private final ShoppingGuideAgent shoppingGuideAgent;
    private final CustomerServiceAgent customerServiceAgent;
    private final SemanticSearchAgent semanticSearchAgent;
    private final RetentionAgent retentionAgent;
    private final MerchantCopywritingAgent copywritingAgent;
    private final CommentManagementAgent commentManagementAgent;
    private final MarketingMaterialAgent marketingMaterialAgent;
    private final OrderDataAnalysisAgent orderDataAnalysisAgent;
    private final AfterSaleTicketAgent afterSaleTicketAgent;
    private final InventorySalesAnalysisAgent inventorySalesAnalysisAgent;
    private final OperationBriefingAgent operationBriefingAgent;
    private final PersonalizedRecommendationAgent recommendationAgent;
    private final SceneMatchAgent sceneMatchAgent;
    private final RagKnowledgeAgent ragKnowledgeAgent;
    private final ObjectMapper objectMapper;

    public MallAgentWorkflow(AgentRouter agentRouter,
                             ContentSafetyAgent contentSafetyAgent,
                             ShoppingGuideAgent shoppingGuideAgent,
                             CustomerServiceAgent customerServiceAgent,
                             SemanticSearchAgent semanticSearchAgent,
                             RetentionAgent retentionAgent,
                             MerchantCopywritingAgent copywritingAgent,
                             CommentManagementAgent commentManagementAgent,
                             MarketingMaterialAgent marketingMaterialAgent,
                             OrderDataAnalysisAgent orderDataAnalysisAgent,
                             AfterSaleTicketAgent afterSaleTicketAgent,
                             InventorySalesAnalysisAgent inventorySalesAnalysisAgent,
                             OperationBriefingAgent operationBriefingAgent,
                             PersonalizedRecommendationAgent recommendationAgent,
                             SceneMatchAgent sceneMatchAgent,
                             RagKnowledgeAgent ragKnowledgeAgent,
                             ObjectMapper objectMapper) {
        this.agentRouter = agentRouter;
        this.contentSafetyAgent = contentSafetyAgent;
        this.shoppingGuideAgent = shoppingGuideAgent;
        this.customerServiceAgent = customerServiceAgent;
        this.semanticSearchAgent = semanticSearchAgent;
        this.retentionAgent = retentionAgent;
        this.copywritingAgent = copywritingAgent;
        this.commentManagementAgent = commentManagementAgent;
        this.marketingMaterialAgent = marketingMaterialAgent;
        this.orderDataAnalysisAgent = orderDataAnalysisAgent;
        this.afterSaleTicketAgent = afterSaleTicketAgent;
        this.inventorySalesAnalysisAgent = inventorySalesAnalysisAgent;
        this.operationBriefingAgent = operationBriefingAgent;
        this.recommendationAgent = recommendationAgent;
        this.sceneMatchAgent = sceneMatchAgent;
        this.ragKnowledgeAgent = ragKnowledgeAgent;
        this.objectMapper = objectMapper;
    }

    public AgentWorkflowResult execute(AgentWorkbenchRequest request) {
        long start = System.currentTimeMillis();
        AgentWorkflowContext context = buildContext(request);
        try {
            String validationError = validateRequest(request);
            if (validationError != null) {
                return failAndLog(context, "AgentWorkbench", AgentErrorCode.PARAM_ERROR, validationError, start);
            }

            ContentSafetyResponse safetyResponse = reviewSafety(context);
            if (isSafetyBlocked(safetyResponse)) {
                return failAndLog(context, "ContentSafetyAgent", AgentErrorCode.SAFETY_BLOCKED,
                        safetyResponse.getReason(), start);
            }

            AgentIntent intent = agentRouter.route(context.getMessage());
            context.setIntent(intent);
            String agentName = agentRouter.agentName(intent);
            if (intent == AgentIntent.UNKNOWN) {
                return failAndLog(context, agentName, AgentErrorCode.INTENT_UNKNOWN,
                        "暂未识别你的需求，请换一种说法。", start);
            }
            if (!hasPermission(context.getRole(), intent)) {
                return failAndLog(context, agentName, AgentErrorCode.PERMISSION_DENIED,
                        "当前角色无权限访问该 Agent：" + intent.name(), start);
            }

            Object data = invokeAgent(context, intent);
            boolean success = readSuccess(data);
            String reply = readReply(data);
            long duration = System.currentTimeMillis() - start;
            AgentWorkflowResult result = AgentWorkflowResult.builder()
                    .success(success)
                    .requestId(context.getRequestId())
                    .sessionId(context.getSessionId())
                    .intent(intent.name())
                    .agentName(agentName)
                    .reply(reply)
                    .data(data)
                    .durationMs(duration)
                    .message(success ? "执行完成" : "目标 Agent 返回失败")
                    .build();
            logExecution(context, agentName, success, duration, success ? null : reply);
            return result;
        } catch (Exception e) {
            return failAndLog(context,
                    context.getIntent() == null ? "UnknownAgent" : agentRouter.agentName(context.getIntent()),
                    AgentErrorCode.TOOL_EXECUTION_ERROR,
                    "工作流执行失败：" + e.getMessage(),
                    start);
        }
    }

    private Object invokeAgent(AgentWorkflowContext context, AgentIntent intent) {
        return switch (intent) {
            case SHOPPING_GUIDE -> shoppingGuideAgent.chat(buildShoppingGuideRequest(context));
            case CUSTOMER_SERVICE -> customerServiceAgent.chat(buildCustomerServiceRequest(context));
            case SEMANTIC_SEARCH -> semanticSearchAgent.search(buildSemanticSearchRequest(context));
            case RETENTION -> retentionAgent.generate(buildRetentionRequest(context));
            case COPYWRITING -> copywritingAgent.generate(buildCopywritingRequest(context));
            case COMMENT_MANAGEMENT -> commentManagementAgent.analyze(buildCommentAnalysisRequest(context));
            case MARKETING -> marketingMaterialAgent.generate(buildMarketingRequest(context));
            case ORDER_ANALYSIS -> orderDataAnalysisAgent.analyze(buildOrderAnalysisRequest());
            case AFTER_SALE -> afterSaleTicketAgent.classify(buildAfterSaleRequest(context));
            case INVENTORY_ANALYSIS -> inventorySalesAnalysisAgent.analyze(buildInventoryAnalysisRequest(context));
            case OPERATION_BRIEFING -> operationBriefingAgent.generate(buildOperationBriefingRequest());
            case PERSONALIZED_RECOMMENDATION -> recommendationAgent.recommend(buildPersonalizedRequest(context));
            case SCENE_MATCH -> sceneMatchAgent.match(buildSceneMatchRequest(context));
            case CONTENT_SAFETY -> contentSafetyAgent.review(buildContentSafetyRequest(context, "AI_CHAT"));
            case RAG_KNOWLEDGE -> ragKnowledgeAgent.ask(buildRagRequest(context));
            case UNKNOWN -> throw new IllegalStateException("未知意图");
        };
    }

    private ContentSafetyResponse reviewSafety(AgentWorkflowContext context) {
        try {
            return contentSafetyAgent.review(buildContentSafetyRequest(context, "AI_CHAT"));
        } catch (Exception e) {
            log.warn("Content safety failed, requestId={}, error={}", context.getRequestId(), e.getMessage());
            return null;
        }
    }

    private boolean isSafetyBlocked(ContentSafetyResponse response) {
        if (response == null || response.getRiskLevel() == null) {
            return false;
        }
        return "BLOCK".equals(response.getSuggestion())
                || "HIGH".equals(response.getRiskLevel())
                || "CRITICAL".equals(response.getRiskLevel());
    }

    private boolean hasPermission(String role, AgentIntent intent) {
        if ("ADMIN".equals(role)) {
            return true;
        }
        return ROLE_PERMISSIONS.getOrDefault(role, Set.of()).contains(intent);
    }

    private AgentWorkflowContext buildContext(AgentWorkbenchRequest request) {
        String role = request == null || !StringUtils.hasText(request.getRole())
                ? "USER"
                : request.getRole().trim().toUpperCase(Locale.ROOT);
        return AgentWorkflowContext.builder()
                .requestId(UUID.randomUUID().toString())
                .userId(request == null ? null : request.getUserId())
                .role(role)
                .sessionId(request == null || !StringUtils.hasText(request.getSessionId())
                        ? "wb-" + UUID.randomUUID()
                        : request.getSessionId())
                .message(request == null ? null : request.getMessage())
                .build();
    }

    private String validateRequest(AgentWorkbenchRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        if (!StringUtils.hasText(request.getMessage())) {
            return "message 不能为空";
        }
        if (!StringUtils.hasText(request.getRole())) {
            return "role 不能为空";
        }
        String role = request.getRole().trim().toUpperCase(Locale.ROOT);
        if (!ROLES.contains(role)) {
            return "role 非法";
        }
        return null;
    }

    private ShoppingGuideRequest buildShoppingGuideRequest(AgentWorkflowContext context) {
        ShoppingGuideRequest request = new ShoppingGuideRequest();
        request.setUserId(context.getUserId());
        request.setSessionId(context.getSessionId());
        request.setMessage(context.getMessage());
        return request;
    }

    private CustomerServiceRequest buildCustomerServiceRequest(AgentWorkflowContext context) {
        CustomerServiceRequest request = new CustomerServiceRequest();
        request.setUserId(context.getUserId());
        request.setSessionId(context.getSessionId());
        request.setMessage(context.getMessage());
        return request;
    }

    private SemanticSearchRequest buildSemanticSearchRequest(AgentWorkflowContext context) {
        SemanticSearchRequest request = new SemanticSearchRequest();
        request.setUserId(context.getUserId());
        request.setKeyword(context.getMessage());
        request.setPage(1);
        request.setSize(10);
        return request;
    }

    private RetentionRequest buildRetentionRequest(AgentWorkflowContext context) {
        RetentionRequest request = new RetentionRequest();
        request.setUserId(context.getUserId());
        request.setSessionId(context.getSessionId());
        request.setEventType("PAGE_EXIT");
        request.setProductId(extractFirstLong(context.getMessage(), 1L));
        request.setMessageContext(context.getMessage());
        return request;
    }

    private CopywritingRequest buildCopywritingRequest(AgentWorkflowContext context) {
        CopywritingRequest request = new CopywritingRequest();
        request.setProductIds(List.of(extractFirstLong(context.getMessage(), 1L)));
        request.setStyle("简洁专业");
        request.setTargetAudience("商城用户");
        request.setScene(context.getMessage());
        request.setGenerateTypes(List.of("TITLE", "SELLING_POINTS", "PROMOTION"));
        return request;
    }

    private CommentAnalysisRequest buildCommentAnalysisRequest(AgentWorkflowContext context) {
        CommentAnalysisRequest request = new CommentAnalysisRequest();
        request.setProductId(extractFirstLong(context.getMessage(), 1L));
        request.setLimit(20);
        request.setAnalysisTypes(List.of("SENTIMENT", "REPLY", "NEGATIVE_SUMMARY"));
        return request;
    }

    private MarketingMaterialRequest buildMarketingRequest(AgentWorkflowContext context) {
        MarketingMaterialRequest request = new MarketingMaterialRequest();
        request.setProductIds(List.of(extractFirstLong(context.getMessage(), 1L)));
        request.setActivityName("商城活动");
        request.setTargetAudience("商城用户");
        request.setStyle("简洁专业");
        request.setChannels(List.of("COMMUNITY", "PUSH", "SLOGAN"));
        request.setExtraRequirement(context.getMessage());
        return request;
    }

    private OrderDataAnalysisRequest buildOrderAnalysisRequest() {
        OrderDataAnalysisRequest request = new OrderDataAnalysisRequest();
        request.setStartDate(LocalDate.now().minusDays(30));
        request.setEndDate(LocalDate.now());
        request.setAnalysisTypes(List.of("FULL_REPORT"));
        request.setCategoryId(null);
        return request;
    }

    private AfterSaleTicketRequest buildAfterSaleRequest(AgentWorkflowContext context) {
        AfterSaleTicketRequest request = new AfterSaleTicketRequest();
        request.setUserId(context.getUserId());
        request.setOrderId(extractFirstLong(context.getMessage(), 1L));
        request.setMessage(context.getMessage());
        request.setImages(List.of());
        return request;
    }

    private InventorySalesAnalysisRequest buildInventoryAnalysisRequest(AgentWorkflowContext context) {
        InventorySalesAnalysisRequest request = new InventorySalesAnalysisRequest();
        request.setStartDate(LocalDate.now().minusDays(30));
        request.setEndDate(LocalDate.now());
        request.setProductIds(List.of(extractFirstLong(context.getMessage(), 1L)));
        request.setCategoryId(null);
        request.setAnalysisTypes(List.of("FULL_REPORT"));
        return request;
    }

    private OperationBriefingRequest buildOperationBriefingRequest() {
        OperationBriefingRequest request = new OperationBriefingRequest();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setBriefingType("WEEKLY");
        request.setSections(List.of("FULL"));
        return request;
    }

    private PersonalizedRecommendationRequest buildPersonalizedRequest(AgentWorkflowContext context) {
        PersonalizedRecommendationRequest request = new PersonalizedRecommendationRequest();
        request.setUserId(context.getUserId());
        request.setScene("HOME_PAGE");
        request.setLimit(10);
        return request;
    }

    private SceneMatchRequest buildSceneMatchRequest(AgentWorkflowContext context) {
        SceneMatchRequest request = new SceneMatchRequest();
        request.setUserId(context.getUserId());
        request.setSceneType(resolveSceneType(context.getMessage()));
        request.setBudget(extractBudget(context.getMessage(), BigDecimal.valueOf(500)));
        request.setMessage(context.getMessage());
        request.setLimit(5);
        return request;
    }

    private ContentSafetyRequest buildContentSafetyRequest(AgentWorkflowContext context, String contentType) {
        ContentSafetyRequest request = new ContentSafetyRequest();
        request.setUserId(context.getUserId());
        request.setContentType(contentType);
        request.setContent(context.getMessage());
        request.setContextId(null);
        return request;
    }

    private RagKnowledgeRequest buildRagRequest(AgentWorkflowContext context) {
        RagKnowledgeRequest request = new RagKnowledgeRequest();
        request.setUserId(context.getUserId());
        request.setQuestion(context.getMessage());
        request.setDocumentType(resolveDocumentType(context.getMessage()));
        request.setTopK(5);
        return request;
    }

    private String resolveSceneType(String message) {
        if (contains(message, "宿舍", "学生")) {
            return "DORMITORY";
        }
        if (contains(message, "手机", "数码", "电脑", "配件")) {
            return "DIGITAL";
        }
        if (contains(message, "礼物", "生日", "礼品")) {
            return "GIFT";
        }
        if (contains(message, "办公", "办公室")) {
            return "OFFICE";
        }
        if (contains(message, "穿搭", "服饰")) {
            return "OUTFIT";
        }
        return "HOME";
    }

    private String resolveDocumentType(String message) {
        if (contains(message, "退货", "换货", "七天无理由")) {
            return "RETURN_RULE";
        }
        if (contains(message, "售后", "破损", "漏发", "错发")) {
            return "AFTER_SALE_POLICY";
        }
        if (contains(message, "优惠", "活动")) {
            return "PROMOTION_RULE";
        }
        if (contains(message, "商品说明", "参数")) {
            return "PRODUCT_MANUAL";
        }
        return null;
    }

    private boolean readSuccess(Object data) {
        JsonNode node = objectMapper.valueToTree(data);
        JsonNode success = node.get("success");
        return success == null || success.asBoolean(true);
    }

    private String readReply(Object data) {
        JsonNode node = objectMapper.valueToTree(data);
        for (String field : List.of("reply", "answer", "message", "summaryReport", "reportMarkdown", "reason", "title")) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual() && StringUtils.hasText(value.asText())) {
                return value.asText();
            }
        }
        return "Agent 已返回结构化结果，请查看 data 字段。";
    }

    private Long extractFirstLong(String text, Long defaultValue) {
        Matcher matcher = NUMBER_PATTERN.matcher(text == null ? "" : text);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private BigDecimal extractBudget(String text, BigDecimal defaultValue) {
        Matcher matcher = BUDGET_PATTERN.matcher(text == null ? "" : text);
        BigDecimal best = null;
        while (matcher.find()) {
            try {
                best = new BigDecimal(matcher.group(1));
            } catch (NumberFormatException ignored) {
                // keep scanning
            }
        }
        return best == null ? defaultValue : best;
    }

    private boolean contains(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private AgentWorkflowResult failAndLog(AgentWorkflowContext context,
                                           String agentName,
                                           AgentErrorCode errorCode,
                                           String message,
                                           long start) {
        long duration = System.currentTimeMillis() - start;
        logExecution(context, agentName, false, duration, message);
        return AgentWorkflowResult.fail(context, agentName, errorCode, message, duration);
    }

    private void logExecution(AgentWorkflowContext context,
                              String agentName,
                              boolean success,
                              long durationMs,
                              String errorMessage) {
        AgentExecutionLog executionLog = AgentExecutionLog.builder()
                .requestId(context == null ? null : context.getRequestId())
                .userId(context == null ? null : context.getUserId())
                .role(context == null ? null : context.getRole())
                .sessionId(context == null ? null : context.getSessionId())
                .intent(context == null || context.getIntent() == null ? null : context.getIntent().name())
                .agentName(agentName)
                .success(success)
                .durationMs(durationMs)
                .errorMessage(errorMessage)
                .createTime(LocalDateTime.now())
                .build();
        log.info("AgentExecutionLog={}", executionLog);
    }
}
