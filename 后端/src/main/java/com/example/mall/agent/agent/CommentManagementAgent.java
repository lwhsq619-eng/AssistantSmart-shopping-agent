package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.CommentAnalysisRequest;
import com.example.mall.agent.dto.agent.CommentAnalysisResponse;
import com.example.mall.agent.dto.agent.CommentReplySuggestion;
import com.example.mall.agent.dto.agent.CommentSummaryReport;
import com.example.mall.agent.dto.tool.CommentToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.CommentManagementPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.CommentTool;
import com.example.mall.agent.tool.ProductTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商家评论智能管理 Agent
 * 业务定位：电商商品评论AI智能分析体
 * 功能：评论情感判断、自动生成商家回复话术、差评问题汇总、全维度评论分析报告
 * 核心约束：仅生成分析建议，不会自动发布评论回复、不修改数据库原始评论数据
 */
@Service
public class CommentManagementAgent {
    // 日志对象，记录接口耗时、正常流程、AI调用异常、业务报错，用于线上排查
    private static final Logger log = LoggerFactory.getLogger(CommentManagementAgent.class);

    // 允许的分析类型集合，限制前端传入参数只能是以下四种
    // SENTIMENT：情感分析  REPLY：生成回复  NEGATIVE_SUMMARY：差评问题汇总  FULL_REPORT：完整综合报告
    private static final Set<String> ALLOWED_TYPES = Set.of("SENTIMENT", "REPLY", "NEGATIVE_SUMMARY", "FULL_REPORT");

    // 评论工具类：查询商品全部评论、单独查询差评列表
    private final CommentTool commentTool;
    // 商品工具类：根据商品ID查询商品基础信息（商品名、规格等，作为AI分析上下文）
    private final ProductTool productTool;
    // AI大模型服务：封装API，提供普通对话chat、深度推理reason两种调用模式
    private final AiService aiService;
    // 评论分析专用提示词管理类，存放固定系统角色Prompt
    private final CommentManagementPrompt commentManagementPrompt;
    // JSON解析工具：解析大模型返回的带Markdown的JSON字符串
    private final ObjectMapper objectMapper;

    /**
     * 构造方法，Spring容器自动注入所有依赖工具与服务
     */
    public CommentManagementAgent(CommentTool commentTool,
                                  ProductTool productTool,
                                  AiService aiService,
                                  CommentManagementPrompt commentManagementPrompt,
                                  ObjectMapper objectMapper) {
        this.commentTool = commentTool;
        this.productTool = productTool;
        this.aiService = aiService;
        this.commentManagementPrompt = commentManagementPrompt;
        this.objectMapper = objectMapper;
    }

    /**
     * 对外唯一入口：商品评论智能分析主方法
     * @param request 前端传入分析请求：商品ID、评论条数限制、需要执行的分析类型
     * @return 标准化分析结果：情感统计、回复话术、差评问题、完整报告
     */
    public CommentAnalysisResponse analyze(CommentAnalysisRequest request) {
        // 1. 校验入参合法性，参数错误直接返回失败响应
        String validationError = validateRequest(request);
        if (validationError != null) {
            return CommentAnalysisResponse.fail(request == null ? null : request.getProductId(), validationError);
        }

        // 记录接口开始时间，用于统计整体耗时
        long start = System.currentTimeMillis();
        // 提取核心参数
        Long productId = request.getProductId();
        // 规范化条数限制（兜底默认50条，限制区间1~200）
        int limit = normalizeLimit(request.getLimit());
        // 过滤、标准化用户传入的分析类型，剔除非法参数
        List<String> analysisTypes = normalizeTypes(request.getAnalysisTypes());

        try {
            // 查询商品基础信息，作为AI分析上下文
            ProductToolResponse product = productTool.getProductDetail(productId);
            // 查询该商品全部评论，条数受limit限制
            List<CommentToolResponse> comments = commentTool.getProductComments(productId, limit);
            // 判断是否需要单独拉取差评：需要差评汇总/完整报告才查询差评列表
            List<CommentToolResponse> negativeComments = needsNegative(analysisTypes)
                    ? commentTool.getNegativeComments(productId, limit)
                    : List.of();

            // 分支：商品无任何评论，直接返回空报告
            if (comments.isEmpty()) {
                CommentSummaryReport emptyReport = buildEmptyReport(product);
                return CommentAnalysisResponse.builder()
                        .success(true)
                        .productId(productId)
                        .sentimentSummary(emptyReport.getSentimentSummary())
                        .replySuggestions(List.of())
                        .negativeProblems(List.of())
                        .summaryReport("当前商品暂无可分析评论数据。")
                        .report(emptyReport)
                        .message("CommentTool 当前未返回评论数据，已按空实现返回。")
                        .build();
            }

            // 核心AI调用逻辑：传入商品+评论数据，获取AI分析结果；AI异常自动返回空结果降级本地规则
            AiCommentResult aiResult = callAiIfNeeded(product, comments, negativeComments, analysisTypes);
            // 构建评论ID -> 情感标签映射（AI结果优先，评分内容冲突时覆盖本地规则）
            Map<Long, String> sentimentMap = buildSentimentMap(comments, aiResult.sentiments());
            // 统计正向/中性/负向评论总数
            Map<String, Integer> sentimentSummary = countSentiments(sentimentMap);
            // 如果开启回复分析，生成每条评论的商家回复建议
            List<CommentReplySuggestion> replySuggestions = analysisTypes.contains("REPLY")
                    ? buildReplySuggestions(comments, sentimentMap, aiResult.replies())
                    : List.of();
            // 如果开启差评汇总，提取所有高频负面问题
            List<String> negativeProblems = needsNegative(analysisTypes)
                    ? buildNegativeProblems(negativeComments.isEmpty() ? comments : negativeComments, aiResult.negativeProblems())
                    : List.of();
            // 如果开启完整报告，拼接AI总结文本；无AI结果则本地自动生成总结文案
            String summaryText = analysisTypes.contains("FULL_REPORT")
                    ? buildSummaryReport(product, comments, sentimentSummary, negativeProblems, aiResult.summaryReport())
                    : "";
            // 组装完整评论汇总报告实体
            CommentSummaryReport report = CommentSummaryReport.builder()
                    .productId(productId)
                    .productName(product.getProductName())
                    .totalCount(comments.size())
                    .sentimentSummary(sentimentSummary)
                    .negativeProblems(negativeProblems)
                    .summaryReport(summaryText)
                    .build();

            // 打印成功日志：商品ID、耗时、评论总数
            log.info("CommentManagementAgent success, productId={}, elapsedMs={}, commentCount={}",
                    productId, System.currentTimeMillis() - start, comments.size());
            // 封装成功响应返回前端
            return CommentAnalysisResponse.builder()
                    .success(true)
                    .productId(productId)
                    .sentimentSummary(sentimentSummary)
                    .replySuggestions(replySuggestions)
                    .negativeProblems(negativeProblems)
                    .summaryReport(summaryText)
                    .report(report)
                    .message("分析完成，回复建议未自动发布。")
                    .build();
        } catch (Exception e) {
            // 全局异常捕获，记录完整错误堆栈，返回统一失败提示
            log.error("CommentManagementAgent failed, productId={}", productId, e);
            return CommentAnalysisResponse.fail(productId, "评论智能分析失败：" + e.getMessage());
        }
    }

    /**
     * AI调用容错封装方法
     * 逻辑：需要深度报告/差评汇总则启用reason深度推理模型；普通情感分析用chat轻量模型
     * AI调用失败、返回空、解析异常全部降级，返回空AiCommentResult，使用本地关键词规则兜底
     */
    private AiCommentResult callAiIfNeeded(ProductToolResponse product,
                                           List<CommentToolResponse> comments,
                                           List<CommentToolResponse> negativeComments,
                                           List<String> analysisTypes) {
        // 无评论直接返回空AI结果，无需调用大模型
        if (comments.isEmpty()) {
            return AiCommentResult.empty();
        }
        // 判断是否启用深度推理：差评汇总/完整综合报告需要高精度分析
        boolean useReasoner = analysisTypes.contains("NEGATIVE_SUMMARY") || analysisTypes.contains("FULL_REPORT");
        try {
            // 拼接完整提示词：输出格式约束 + 商品上下文 + 全部评论 + 业务限制规则
            String prompt = buildPrompt(product, comments, negativeComments, analysisTypes);
            // 区分模型调用方式
            AiChatResponse aiResponse = useReasoner
                    ? aiService.reason(commentManagementPrompt.systemPrompt(), prompt)
                    : aiService.chat(commentManagementPrompt.systemPrompt(), prompt);
            // AI接口调用失败 / 返回空白内容，直接降级本地规则
            if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
                return AiCommentResult.empty();
            }
            // 解析AI返回的JSON文本，封装结构化结果
            return parseAiResult(aiResponse.getContent());
        } catch (Exception e) {
            // AI调用异常，打印警告日志，返回空结果降级
            log.warn("CommentManagementAgent AI fallback, productId={}, error={}", product.getProductId(), e.getMessage());
            return AiCommentResult.empty();
        }
    }

    /**
     * 构造发给大模型的完整Prompt
     * 约束：强制输出固定JSON结构，禁止自动承诺赔偿、退款、赠品
     */
    private String buildPrompt(ProductToolResponse product,
                               List<CommentToolResponse> comments,
                               List<CommentToolResponse> negativeComments,
                               List<String> analysisTypes) {
        return "请只返回 JSON，格式：{\"sentimentResults\":[{\"commentId\":1,\"sentiment\":\"NEGATIVE\",\"reason\":\"\"}],"
                + "\"replySuggestions\":[{\"commentId\":1,\"suggestedReply\":\"\"}],"
                + "\"negativeProblems\":[],\"summaryReport\":\"\"}\n"
                + "分析类型：" + analysisTypes + "\n"
                + "商品信息：" + product + "\n"
                + "评论列表：" + comments + "\n"
                + "差评列表：" + negativeComments + "\n"
                + "要求：不自动发布回复；不承诺退款、赔偿、赠品；只基于提供的评论内容分析。";
    }

    /**
     * 解析大模型返回原始文本，提取JSON并封装AI结构化结果
     * 输出：情感判断集合、回复话术集合、差评问题列表、综合总结文本
     */
    private AiCommentResult parseAiResult(String content) throws Exception {
        // 提取纯净JSON字符串，去除markdown ```json 代码块标记
        JsonNode root = objectMapper.readTree(extractJson(content));
        // 存储每条评论AI情感判断：key=评论ID value=情感+判断理由
        Map<Long, SentimentDecision> sentiments = new LinkedHashMap<>();
        JsonNode sentimentResults = root.get("sentimentResults");
        // 遍历AI返回的情感分析数组
        if (sentimentResults != null && sentimentResults.isArray()) {
            for (JsonNode item : sentimentResults) {
                Long commentId = item.hasNonNull("commentId") ? item.get("commentId").asLong() : null;
                String sentiment = item.hasNonNull("sentiment") ? normalizeSentiment(item.get("sentiment").asText()) : null;
                String reason = item.hasNonNull("reason") ? item.get("reason").asText("") : "";
                // 评论ID和情感标签合法才存入map
                if (commentId != null && sentiment != null) {
                    sentiments.put(commentId, new SentimentDecision(sentiment, reason));
                }
            }
        }

        // 存储AI生成的回复话术：key=评论ID value=回复文案
        Map<Long, String> replies = new LinkedHashMap<>();
        JsonNode replySuggestions = root.get("replySuggestions");
        if (replySuggestions != null && replySuggestions.isArray()) {
            for (JsonNode item : replySuggestions) {
                if (item.hasNonNull("commentId") && item.hasNonNull("suggestedReply")) {
                    // 清洗回复文案，过滤违规承诺词汇
                    replies.put(item.get("commentId").asLong(), sanitizeReply(item.get("suggestedReply").asText("")));
                }
            }
        }

        // 存储AI提取的高频负面问题
        List<String> negativeProblems = new ArrayList<>();
        JsonNode problems = root.get("negativeProblems");
        if (problems != null && problems.isArray()) {
            for (JsonNode item : problems) {
                if (StringUtils.hasText(item.asText())) {
                    negativeProblems.add(item.asText());
                }
            }
        }
        // 综合总结报告文本
        String summary = root.hasNonNull("summaryReport") ? root.get("summaryReport").asText("") : "";
        return new AiCommentResult(sentiments, replies, negativeProblems, summary);
    }

    /**
     * 构建最终情感映射表：AI情感结果优先级高于本地评分规则
     * 特殊逻辑：评论星级与文本情绪冲突时，强制使用AI判断结果（解决高分差评、低分好评矛盾场景）
     */
    private Map<Long, String> buildSentimentMap(List<CommentToolResponse> comments, Map<Long, SentimentDecision> aiSentiments) {
        Map<Long, String> result = new LinkedHashMap<>();
        for (CommentToolResponse comment : comments) {
            // 本地评分关键词规则得出基础情感
            String ruleSentiment = judgeByRule(comment);
            // 获取该评论AI情感判断
            SentimentDecision aiDecision = aiSentiments.get(comment.getCommentId());
            // 判断星级和文本情绪是否冲突
            boolean conflict = hasContentRatingConflict(comment);
            // 冲突场景优先AI结果，无冲突使用本地规则
            String sentiment = conflict && aiDecision != null ? aiDecision.sentiment() : ruleSentiment;
            result.put(comment.getCommentId(), sentiment);
        }
        return result;
    }

    /**
     * 本地规则情感判断（AI服务不可用时兜底降级逻辑）
     * 判断依据：星级评分 + 文本关键词
     * POSITIVE正向 / NEGATIVE负向 / NEUTRAL中性
     */
    private String judgeByRule(CommentToolResponse comment) {
        Integer rating = comment.getRating();
        // 优先根据星级判断
        if (rating != null) {
            if (rating <= 2) {
                return "NEGATIVE";
            }
            if (rating >= 4) {
                return "POSITIVE";
            }
        }
        String content = safe(comment.getContent());
        // 负面关键词匹配
        if (containsAny(content, "差", "坏", "破", "慢", "不满意", "失望", "退货", "投诉")) {
            return "NEGATIVE";
        }
        // 正面关键词匹配
        if (containsAny(content, "好", "满意", "喜欢", "不错", "推荐", "还会买")) {
            return "POSITIVE";
        }
        // 无匹配关键词判定中性
        return "NEUTRAL";
    }

    /**
     * 判断评论星级与文本情绪是否冲突
     * 冲突场景：高分4-5星但文字差评 / 低分1-2星但文字好评
     */
    private boolean hasContentRatingConflict(CommentToolResponse comment) {
        Integer rating = comment.getRating();
        // 无星级/无评论文本不冲突
        if (rating == null || !StringUtils.hasText(comment.getContent())) {
            return false;
        }
        String content = comment.getContent();
        boolean negativeText = containsAny(content, "差", "坏", "破", "慢", "不满意", "失望", "退货", "投诉");
        boolean positiveText = containsAny(content, "好", "满意", "喜欢", "不错", "推荐", "还会买");
        // 高分差评 或 低分好评 = 冲突
        return (rating >= 4 && negativeText) || (rating <= 2 && positiveText);
    }

    /**
     * 生成每条评论的商家回复建议
     * AI无返回文案时，根据情感标签使用本地固定兜底回复
     */
    private List<CommentReplySuggestion> buildReplySuggestions(List<CommentToolResponse> comments,
                                                               Map<Long, String> sentimentMap,
                                                               Map<Long, String> aiReplies) {
        List<CommentReplySuggestion> suggestions = new ArrayList<>();
        for (CommentToolResponse comment : comments) {
            String sentiment = sentimentMap.getOrDefault(comment.getCommentId(), judgeByRule(comment));
            // 优先取AI生成回复，为空则使用兜底模板
            String reply = aiReplies.get(comment.getCommentId());
            if (!StringUtils.hasText(reply)) {
                reply = fallbackReply(sentiment);
            }
            // 封装回复建议实体
            suggestions.add(CommentReplySuggestion.builder()
                    .commentId(comment.getCommentId())
                    .sentiment(sentiment)
                    .suggestedReply(sanitizeReply(reply))
                    .reason("仅生成审核建议，未自动发布")
                    .build());
        }
        return suggestions;
    }

    /**
     * 汇总所有差评高频问题（AI结果 + 本地关键词双重汇总，自动去重）
     */
    private List<String> buildNegativeProblems(List<CommentToolResponse> comments, List<String> aiProblems) {
        // LinkedHashSet自动去重，保留插入顺序
        Set<String> problems = new LinkedHashSet<>();
        // 先放入AI提取的问题
        if (aiProblems != null) {
            problems.addAll(aiProblems);
        }
        // 本地关键词二次提取负面问题
        for (CommentToolResponse comment : comments) {
            // 只处理判定为负向的评论
            if (!"NEGATIVE".equals(judgeByRule(comment))) {
                continue;
            }
            String content = safe(comment.getContent());
            if (containsAny(content, "物流", "快递", "发货慢")) {
                problems.add("物流问题");
            }
            if (containsAny(content, "坏", "破", "质量差")) {
                problems.add("商品质量");
            }
            if (containsAny(content, "不合适", "尺寸", "规格")) {
                problems.add("尺寸/规格不符");
            }
            if (containsAny(content, "客服", "态度")) {
                problems.add("客服态度");
            }
            if (containsAny(content, "贵", "降价")) {
                problems.add("价格问题");
            }
            if (containsAny(content, "包装", "破损")) {
                problems.add("包装破损");
            }
            if (containsAny(content, "不好用", "体验差", "难用")) {
                problems.add("使用体验差");
            }
        }
        // 无任何负面问题填充默认值
        if (problems.isEmpty()) {
            problems.add("其他");
        }
        return new ArrayList<>(problems);
    }

    /**
     * 统计正向、中性、负向评论数量
     */
    private Map<String, Integer> countSentiments(Map<Long, String> sentimentMap) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        // 初始化三类统计值为0
        summary.put("positiveCount", 0);
        summary.put("neutralCount", 0);
        summary.put("negativeCount", 0);
        // 遍历情感map累加计数
        for (String sentiment : sentimentMap.values()) {
            if ("POSITIVE".equals(sentiment)) {
                summary.compute("positiveCount", (k, v) -> v + 1);
            } else if ("NEGATIVE".equals(sentiment)) {
                summary.compute("negativeCount", (k, v) -> v + 1);
            } else {
                summary.compute("neutralCount", (k, v) -> v + 1);
            }
        }
        return summary;
    }

    /**
     * 生成综合评论总结报告
     * AI有返回文本优先使用AI总结；无AI结果则本地拼接统计文案
     */
    private String buildSummaryReport(ProductToolResponse product,
                                      List<CommentToolResponse> comments,
                                      Map<String, Integer> sentimentSummary,
                                      List<String> negativeProblems,
                                      String aiSummary) {
        if (StringUtils.hasText(aiSummary)) {
            return aiSummary;
        }
        // 本地兜底总结文案
        return "商品「" + product.getProductName() + "」共分析 " + comments.size()
                + " 条评论，正向 " + sentimentSummary.get("positiveCount")
                + " 条，中性 " + sentimentSummary.get("neutralCount")
                + " 条，负向 " + sentimentSummary.get("negativeCount")
                + " 条。主要负面问题：" + String.join("、", negativeProblems) + "。";
    }

    /**
     * 构建无评论时的空报告实体
     */
    private CommentSummaryReport buildEmptyReport(ProductToolResponse product) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("positiveCount", 0);
        summary.put("neutralCount", 0);
        summary.put("negativeCount", 0);
        return CommentSummaryReport.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .totalCount(0)
                .sentimentSummary(summary)
                .negativeProblems(List.of())
                .summaryReport("当前商品暂无可分析评论数据。")
                .build();
    }

    /**
     * 本地兜底回复模板，AI未生成回复时使用
     * 按情感标签区分话术
     */
    private String fallbackReply(String sentiment) {
        return switch (sentiment) {
            case "POSITIVE" -> "感谢您的支持，期待您再次光临。";
            case "NEGATIVE" -> "很抱歉给您带来不好的体验，建议您联系在线客服进一步处理。";
            default -> "感谢您的反馈，我们会持续优化商品和服务体验。";
        };
    }

    /**
     * 请求入参全局校验
     * 返回null代表校验通过，返回字符串代表错误信息
     */
    private String validateRequest(CommentAnalysisRequest request) {
        if (request == null || request.getProductId() == null) {
            return "productId 不能为空。";
        }
        // 条数下限校验
        if (request.getLimit() != null && request.getLimit() < 1) {
            return "limit 不能小于 1。";
        }
        // 条数上限校验
        if (request.getLimit() != null && request.getLimit() > 200) {
            return "limit 不能超过 200。";
        }
        // 分析类型不能为空
        if (request.getAnalysisTypes() == null || request.getAnalysisTypes().isEmpty()) {
            return "analysisTypes 不能为空。";
        }
        // 过滤后无合法分析类型
        if (normalizeTypes(request.getAnalysisTypes()).isEmpty()) {
            return "analysisTypes 不合法。";
        }
        return null;
    }

    /**
     * 标准化分析类型：去空格、转大写、过滤非法类型，自动去重
     */
    private List<String> normalizeTypes(List<String> types) {
        Set<String> result = new LinkedHashSet<>();
        for (String type : types) {
            if (!StringUtils.hasText(type)) {
                continue;
            }
            String normalized = type.trim().toUpperCase();
            // 只保留ALLOWED_TYPES内合法类型
            if (ALLOWED_TYPES.contains(normalized)) {
                result.add(normalized);
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 判断当前分析需求是否需要查询差评列表
     * 差评汇总 / 完整综合报告 两种场景需要
     */
    private boolean needsNegative(List<String> analysisTypes) {
        return analysisTypes.contains("NEGATIVE_SUMMARY") || analysisTypes.contains("FULL_REPORT");
    }

    /**
     * 规范化评论查询条数limit
     * null默认50，强制限制区间1~200
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        // 限制最小值1，最大值200
        return Math.min(Math.max(limit, 1), 200);
    }

    /**
     * 标准化情感标签，非法值返回null
     */
    private String normalizeSentiment(String sentiment) {
        String value = sentiment == null ? "" : sentiment.trim().toUpperCase();
        if ("POSITIVE".equals(value) || "NEUTRAL".equals(value) || "NEGATIVE".equals(value)) {
            return value;
        }
        return null;
    }

    /**
     * 清洗AI生成的回复文案，过滤违规承诺词汇，规避客诉风险
     */
    private String sanitizeReply(String reply) {
        if (!StringUtils.hasText(reply)) {
            return reply;
        }
        return reply.replace("赔偿", "协助处理")
                .replace("退款给您", "建议联系售后进一步处理")
                .replace("补发赠品", "由客服核实后处理");
    }

    /**
     * 从大模型返回混合文本中提取纯净JSON
     * 去除```json代码块标记，截取第一个{到最后一个}
     */
    private String extractJson(String content) {
        String text = content.trim();
        // 移除markdown代码块标识
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 多关键词匹配工具：文本包含任意一个关键词返回true
     */
    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 空值兼容工具：null字符串转为空串，防止空指针
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 内部记录：单条评论AI情感决策（情感标签+判断理由）
     */
    private record SentimentDecision(String sentiment, String reason) {
    }

    /**
     * AI返回完整结构化结果封装记录
     * sentiments：评论ID-情感映射
     * replies：评论ID-回复话术映射
     * negativeProblems：差评高频问题列表
     * summaryReport：综合分析报告文本
     */
    private record AiCommentResult(Map<Long, SentimentDecision> sentiments,
                                   Map<Long, String> replies,
                                   List<String> negativeProblems,
                                   String summaryReport) {
        /**
         * 空AI结果静态方法，AI调用失败时降级使用
         */
        private static AiCommentResult empty() {
            return new AiCommentResult(Map.of(), Map.of(), List.of(), "");
        }
    }
}