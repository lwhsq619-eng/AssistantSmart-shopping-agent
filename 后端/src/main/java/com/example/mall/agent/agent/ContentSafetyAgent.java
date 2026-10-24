package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.ContentSafetyRequest;
import com.example.mall.agent.dto.agent.ContentSafetyResponse;
import com.example.mall.agent.dto.agent.ContentSafetyResult;
import com.example.mall.agent.prompt.ContentSafetyPrompt;
import com.example.mall.agent.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 内容安全审核 Agent。
 * 只返回审核结果和处理建议，不自动删除评论、不封禁用户、不修改业务数据。
 */
@Service
public class ContentSafetyAgent {
    private static final Logger log = LoggerFactory.getLogger(ContentSafetyAgent.class);
    private static final Set<String> CONTENT_TYPES = Set.of(
            "COMMENT",
            "PRIVATE_MESSAGE",
            "AI_CHAT",
            "PRODUCT_COPY",
            "MARKETING_COPY",
            "CUSTOMER_SERVICE_MESSAGE"
    );
    private static final List<String> RISK_ORDER = List.of("NONE", "LOW", "MEDIUM", "HIGH", "CRITICAL");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(?<!\\d)\\d{17}[0-9Xx](?!\\d)");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(?<!\\d)\\d{16,19}(?!\\d)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private final AiService aiService;
    private final ContentSafetyPrompt contentSafetyPrompt;
    private final ObjectMapper objectMapper;

    public ContentSafetyAgent(AiService aiService,
                              ContentSafetyPrompt contentSafetyPrompt,
                              ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.contentSafetyPrompt = contentSafetyPrompt;
        this.objectMapper = objectMapper;
    }

    public ContentSafetyResponse review(ContentSafetyRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return ContentSafetyResponse.fail(request == null ? null : request.getContentType(), validationError);
        }

        String contentType = request.getContentType().trim().toUpperCase(Locale.ROOT);
        long start = System.currentTimeMillis();
        try {
            ContentSafetyResult javaResult = javaScreen(contentType, request.getContent());
            ContentSafetyResult aiResult = null;
            boolean fallbackUsed = false;

            if (shouldCallAi(contentType, request.getContent(), javaResult)) {
                try {
                    aiResult = callAiReview(request, contentType, javaResult);
                } catch (Exception e) {
                    fallbackUsed = true;
                    log.warn("ContentSafetyAgent AI fallback, contentType={}, error={}", contentType, e.getMessage());
                }
            }

            ContentSafetyResult finalResult = mergeResults(javaResult, aiResult);
            log.info("ContentSafetyAgent success, contentType={}, riskLevel={}, suggestion={}, elapsedMs={}",
                    contentType, finalResult.getRiskLevel(), finalResult.getSuggestion(), System.currentTimeMillis() - start);
            return ContentSafetyResponse.builder()
                    .success(true)
                    .contentType(contentType)
                    .safe(finalResult.getSafe())
                    .riskLevel(finalResult.getRiskLevel())
                    .categories(finalResult.getCategories())
                    .suggestion(finalResult.getSuggestion())
                    .reason(finalResult.getReason())
                    .javaResult(javaResult)
                    .aiResult(aiResult)
                    .fallbackUsed(fallbackUsed)
                    .message("审核完成，仅返回建议，未修改任何业务数据。")
                    .build();
        } catch (Exception e) {
            log.error("ContentSafetyAgent failed, contentType={}", contentType, e);
            return ContentSafetyResponse.fail(contentType, "内容安全审核失败：" + e.getMessage());
        }
    }

    private ContentSafetyResult javaScreen(String contentType, String content) {
        Set<String> categories = new LinkedHashSet<>();
        List<String> reasons = new ArrayList<>();
        String normalized = content.toLowerCase(Locale.ROOT);

        if (containsAny(content, "傻逼", "脑残", "废物", "滚开", "去死", "你妈", "蠢货")) {
            categories.add("ABUSE");
            reasons.add("命中辱骂攻击词");
        }
        if (containsAny(content, "骚扰", "裸聊", "约吗", "一直联系你")) {
            categories.add("HARASSMENT");
            reasons.add("命中骚扰相关词");
        }
        if (containsAny(content, "刷单", "套现", "绕过平台规则", "骗保", "骗退款", "私聊返现")) {
            categories.add("FRAUD");
            reasons.add("命中诈骗或规避平台规则相关词");
        }
        if (containsAny(content, "加微信", "加我微信", "站外交易", "别在平台下单", "私下交易", "私聊返现")) {
            categories.add("SPAM");
            reasons.add("命中站外交易或垃圾广告特征");
        }
        if (containsAny(content, "违法", "违禁品", "毒品", "枪支", "假证", "洗钱")) {
            categories.add("ILLEGAL");
            reasons.add("命中违法违规相关词");
        }
        if (containsAny(content, "色情", "低俗", "裸聊", "约炮", "成人视频")) {
            categories.add("PORN");
            reasons.add("命中色情低俗相关词");
        }
        if (containsAny(content, "杀了", "砍死", "炸掉", "打死", "威胁你")) {
            categories.add("VIOLENCE");
            reasons.add("命中暴力威胁相关词");
        }
        if (containsPrivacy(content)) {
            categories.add("PRIVACY_LEAK");
            reasons.add("检测到手机号、身份证号、银行卡号或邮箱等隐私信息");
        }
        if (containsAny(content, "政治敏感", "涉政")) {
            categories.add("POLITICAL_SENSITIVE");
            reasons.add("命中涉政敏感表达");
        }
        if (containsAny(content, "全网最低", "永久有效", "100%有效", "包治百病", "稳赚不赔")) {
            categories.add("FAKE_PROMOTION");
            reasons.add("命中虚假营销或违规承诺词");
        }
        if (containsAny(content, "绝对第一", "第一品牌", "最强", "唯一", "顶级效果")) {
            categories.add("EXTREME_CLAIM");
            reasons.add("命中绝对化或夸大表达");
        }
        if (categories.isEmpty() && isNegativeReview(contentType, normalized)) {
            categories.add("NEGATIVE_REVIEW");
            reasons.add("属于普通负面评价，未命中明确违规内容");
        }
        if (categories.isEmpty()) {
            categories.add("SAFE");
            reasons.add("Java 初筛未命中风险关键词");
        }

        String riskLevel = resolveJavaRisk(categories);
        String suggestion = decideSuggestion(riskLevel, categories);
        return ContentSafetyResult.builder()
                .safe("ALLOW".equals(suggestion))
                .riskLevel(riskLevel)
                .categories(new ArrayList<>(categories))
                .suggestion(suggestion)
                .reason(String.join("；", reasons))
                .source("JAVA_RULE")
                .build();
    }

    private ContentSafetyResult callAiReview(ContentSafetyRequest request,
                                             String contentType,
                                             ContentSafetyResult javaResult) throws Exception {
        String prompt = "请对输入内容做安全审核，只返回 JSON。\n"
                + "contentType：" + contentType + "\n"
                + "userId：" + request.getUserId() + "\n"
                + "contextId：" + request.getContextId() + "\n"
                + "Java 初筛结果：" + toJson(javaResult) + "\n"
                + "内容：" + request.getContent() + "\n"
                + "JSON 格式：{\"safe\":true,\"riskLevel\":\"LOW\",\"categories\":[\"NEGATIVE_REVIEW\"],\"suggestion\":\"ALLOW\",\"reason\":\"\"}";
        AiChatResponse aiResponse = shouldUseReasoner(javaResult, request.getContent())
                ? aiService.reason(contentSafetyPrompt.systemPrompt(), prompt)
                : aiService.chat(contentSafetyPrompt.systemPrompt(), prompt);
        if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
            throw new IllegalStateException(aiResponse.getMessage() == null ? "AI 审核无有效返回" : aiResponse.getMessage());
        }
        return parseAiResult(aiResponse.getContent());
    }

    private ContentSafetyResult parseAiResult(String content) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(content));
        List<String> categories = readCategories(root.get("categories"));
        if (categories.isEmpty()) {
            categories = List.of("SAFE");
        }
        String riskLevel = normalizeRisk(root.path("riskLevel").asText("NONE"));
        String suggestion = normalizeSuggestion(root.path("suggestion").asText(decideSuggestion(riskLevel, new LinkedHashSet<>(categories))));
        return ContentSafetyResult.builder()
                .safe("ALLOW".equals(suggestion))
                .riskLevel(riskLevel)
                .categories(categories)
                .suggestion(suggestion)
                .reason(root.path("reason").asText("AI 精审完成"))
                .source("DEEPSEEK")
                .build();
    }

    private ContentSafetyResult mergeResults(ContentSafetyResult javaResult, ContentSafetyResult aiResult) {
        if (aiResult == null) {
            return javaResult;
        }
        Set<String> categories = new LinkedHashSet<>();
        categories.addAll(javaResult.getCategories());
        categories.addAll(aiResult.getCategories());
        categories.remove("SAFE");
        if (categories.isEmpty()) {
            categories.add("SAFE");
        }

        String riskLevel = maxRisk(javaResult.getRiskLevel(), aiResult.getRiskLevel());
        String suggestion = decideSuggestion(riskLevel, categories);
        String reason = "Java 初筛：" + javaResult.getReason() + "；AI 精审：" + aiResult.getReason();
        return ContentSafetyResult.builder()
                .safe("ALLOW".equals(suggestion))
                .riskLevel(riskLevel)
                .categories(new ArrayList<>(categories))
                .suggestion(suggestion)
                .reason(reason)
                .source("MERGED")
                .build();
    }

    private boolean shouldCallAi(String contentType, String content, ContentSafetyResult javaResult) {
        return riskRank(javaResult.getRiskLevel()) >= riskRank("MEDIUM")
                || content.length() > 50
                || Set.of("PRODUCT_COPY", "MARKETING_COPY", "AI_CHAT", "CUSTOMER_SERVICE_MESSAGE").contains(contentType)
                || containsAny(content, "投诉", "维权", "争议", "赔偿", "承诺");
    }

    private boolean shouldUseReasoner(ContentSafetyResult javaResult, String content) {
        return riskRank(javaResult.getRiskLevel()) >= riskRank("HIGH")
                || containsAny(content, "投诉", "维权", "违法", "政治", "赔偿", "责任");
    }

    private String resolveJavaRisk(Set<String> categories) {
        if (hasAny(categories, "ILLEGAL", "PORN", "VIOLENCE", "PRIVACY_LEAK", "POLITICAL_SENSITIVE")) {
            return "CRITICAL";
        }
        if (hasAny(categories, "FRAUD", "SPAM")) {
            return "HIGH";
        }
        if (hasAny(categories, "FAKE_PROMOTION", "EXTREME_CLAIM", "HARASSMENT")) {
            return "MEDIUM";
        }
        if (hasAny(categories, "ABUSE", "NEGATIVE_REVIEW")) {
            return "LOW";
        }
        return "NONE";
    }

    private String decideSuggestion(String riskLevel, Set<String> categories) {
        if ("CRITICAL".equals(riskLevel)) {
            return "BLOCK";
        }
        if ("HIGH".equals(riskLevel)) {
            return hasAny(categories, "ILLEGAL", "PORN", "VIOLENCE", "PRIVACY_LEAK") ? "BLOCK" : "MANUAL_REVIEW";
        }
        if ("MEDIUM".equals(riskLevel)) {
            return hasAny(categories, "FAKE_PROMOTION", "EXTREME_CLAIM", "SPAM", "FRAUD") ? "MANUAL_REVIEW" : "WARN";
        }
        if ("LOW".equals(riskLevel)) {
            return categories.size() == 1 && categories.contains("NEGATIVE_REVIEW") ? "ALLOW" : "WARN";
        }
        return "ALLOW";
    }

    private boolean isNegativeReview(String contentType, String content) {
        return Set.of("COMMENT", "CUSTOMER_SERVICE_MESSAGE").contains(contentType)
                && containsAny(content, "不好用", "物流慢", "太差", "很差", "垃圾", "不满意", "客服差");
    }

    private boolean containsPrivacy(String content) {
        return PHONE_PATTERN.matcher(content).find()
                || ID_CARD_PATTERN.matcher(content).find()
                || BANK_CARD_PATTERN.matcher(content).find()
                || EMAIL_PATTERN.matcher(content).find();
    }

    private boolean containsAny(String content, String... keywords) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAny(Set<String> categories, String... values) {
        for (String value : values) {
            if (categories.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private List<String> readCategories(JsonNode node) {
        List<String> categories = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String category = item.asText("").trim().toUpperCase(Locale.ROOT);
                if (StringUtils.hasText(category)) {
                    categories.add(category);
                }
            }
        }
        return categories;
    }

    private String normalizeRisk(String value) {
        String risk = value == null ? "NONE" : value.trim().toUpperCase(Locale.ROOT);
        return RISK_ORDER.contains(risk) ? risk : "NONE";
    }

    private String normalizeSuggestion(String value) {
        String suggestion = value == null ? "ALLOW" : value.trim().toUpperCase(Locale.ROOT);
        return Set.of("ALLOW", "WARN", "MANUAL_REVIEW", "BLOCK").contains(suggestion) ? suggestion : "MANUAL_REVIEW";
    }

    private String maxRisk(String left, String right) {
        return riskRank(left) >= riskRank(right) ? normalizeRisk(left) : normalizeRisk(right);
    }

    private int riskRank(String riskLevel) {
        return RISK_ORDER.indexOf(normalizeRisk(riskLevel));
    }

    private String validateRequest(ContentSafetyRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        if (!StringUtils.hasText(request.getContentType())) {
            return "contentType 不能为空";
        }
        if (!CONTENT_TYPES.contains(request.getContentType().trim().toUpperCase(Locale.ROOT))) {
            return "contentType 非法";
        }
        if (!StringUtils.hasText(request.getContent())) {
            return "content 不能为空";
        }
        if (request.getContent().length() > 5000) {
            return "content 不能超过 5000 字";
        }
        return null;
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return String.valueOf(object);
        }
    }
}
