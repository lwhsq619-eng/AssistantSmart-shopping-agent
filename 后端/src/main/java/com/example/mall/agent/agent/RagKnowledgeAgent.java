package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.RagKnowledgeRequest;
import com.example.mall.agent.dto.agent.RagKnowledgeResponse;
import com.example.mall.agent.dto.agent.RagSourceChunk;
import com.example.mall.agent.prompt.RagKnowledgePrompt;
import com.example.mall.agent.rag.RagSearchService;
import com.example.mall.agent.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RAG 知识库问答 Agent。
 * 无检索依据时不调用 DeepSeek，避免编造平台规则。
 */
@Service
public class RagKnowledgeAgent {
    private static final Logger log = LoggerFactory.getLogger(RagKnowledgeAgent.class);
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "PRODUCT_MANUAL",
            "AFTER_SALE_POLICY",
            "RETURN_RULE",
            "PROMOTION_RULE",
            "PLATFORM_HELP",
            "OTHER"
    );
    private static final String NO_EVIDENCE_TEXT = "知识库中暂无相关依据，建议联系人工客服确认。";

    private final RagSearchService searchService;
    private final AiService aiService;
    private final RagKnowledgePrompt ragKnowledgePrompt;

    public RagKnowledgeAgent(RagSearchService searchService,
                             AiService aiService,
                             RagKnowledgePrompt ragKnowledgePrompt) {
        this.searchService = searchService;
        this.aiService = aiService;
        this.ragKnowledgePrompt = ragKnowledgePrompt;
    }

    public RagKnowledgeResponse ask(RagKnowledgeRequest request) {
        String validationError = validateRequest(request);
        if (validationError != null) {
            return RagKnowledgeResponse.fail(validationError);
        }

        String documentType = normalizeDocumentType(request.getDocumentType());
        int topK = normalizeTopK(request.getTopK());
        long start = System.currentTimeMillis();
        try {
            List<RagSourceChunk> sources = searchService.search(request.getQuestion(), documentType, topK);
            if (!searchService.hasEvidence(sources)) {
                return RagKnowledgeResponse.builder()
                        .success(true)
                        .answer(NO_EVIDENCE_TEXT)
                        .sources(sources)
                        .noEvidence(true)
                        .fallbackUsed(false)
                        .message("未检索到足够知识依据，未调用 AI 生成答案。")
                        .build();
            }

            AiAnswer aiAnswer = generateAnswer(request, documentType, sources);
            log.info("RagKnowledgeAgent success, documentType={}, topK={}, elapsedMs={}, sourceCount={}",
                    documentType, topK, System.currentTimeMillis() - start, sources.size());
            return RagKnowledgeResponse.builder()
                    .success(true)
                    .answer(aiAnswer.answer())
                    .sources(sources)
                    .noEvidence(false)
                    .fallbackUsed(aiAnswer.fallbackUsed())
                    .message("问答完成，回答基于知识库检索片段生成。")
                    .build();
        } catch (Exception e) {
            log.error("RagKnowledgeAgent failed, documentType={}", documentType, e);
            return RagKnowledgeResponse.fail("知识库问答失败：" + e.getMessage());
        }
    }

    private AiAnswer generateAnswer(RagKnowledgeRequest request, String documentType, List<RagSourceChunk> sources) {
        try {
            String prompt = buildPrompt(request, documentType, sources);
            AiChatResponse response = isComplexPolicyQuestion(request.getQuestion(), documentType)
                    ? aiService.reason(ragKnowledgePrompt.systemPrompt(), prompt)
                    : aiService.chat(ragKnowledgePrompt.systemPrompt(), prompt);
            if (!Boolean.TRUE.equals(response.getSuccess()) || !StringUtils.hasText(response.getContent())) {
                return fallbackAnswer(sources);
            }
            return new AiAnswer(response.getContent().trim(), false);
        } catch (Exception e) {
            log.warn("RagKnowledgeAgent AI fallback, error={}", e.getMessage());
            return fallbackAnswer(sources);
        }
    }

    private String buildPrompt(RagKnowledgeRequest request, String documentType, List<RagSourceChunk> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：").append(request.getQuestion()).append("\n");
        builder.append("文档类型：").append(documentType == null ? "不限" : documentType).append("\n");
        builder.append("知识库片段：\n");
        for (int i = 0; i < sources.size(); i++) {
            RagSourceChunk source = sources.get(i);
            builder.append("片段").append(i + 1)
                    .append(" documentId=").append(source.getDocumentId())
                    .append(", chunkId=").append(source.getChunkId())
                    .append(", score=").append(source.getScore())
                    .append("\n")
                    .append(source.getContent())
                    .append("\n");
        }
        builder.append("要求：只能基于上述片段回答；依据不足时必须回答“").append(NO_EVIDENCE_TEXT).append("”。");
        return builder.toString();
    }

    private AiAnswer fallbackAnswer(List<RagSourceChunk> sources) {
        if (sources == null || sources.isEmpty()) {
            return new AiAnswer(NO_EVIDENCE_TEXT, true);
        }
        String content = sources.get(0).getContent();
        String summary = content.length() > 220 ? content.substring(0, 220) + "..." : content;
        return new AiAnswer("根据知识库最相关片段：" + summary, true);
    }

    private boolean isComplexPolicyQuestion(String question, String documentType) {
        return "AFTER_SALE_POLICY".equals(documentType)
                || "RETURN_RULE".equals(documentType)
                || question.contains("退款")
                || question.contains("维权")
                || question.contains("售后")
                || question.contains("退货")
                || question.contains("换货");
    }

    private String validateRequest(RagKnowledgeRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        if (!StringUtils.hasText(request.getQuestion())) {
            return "question 不能为空";
        }
        if (request.getTopK() != null && (request.getTopK() < 1 || request.getTopK() > 10)) {
            return "topK 必须在 1 到 10 之间";
        }
        String documentType = normalizeDocumentType(request.getDocumentType());
        if (documentType != null && !DOCUMENT_TYPES.contains(documentType)) {
            return "documentType 非法";
        }
        return null;
    }

    private int normalizeTopK(Integer topK) {
        if (topK == null) {
            return 5;
        }
        return Math.min(Math.max(topK, 1), 10);
    }

    private String normalizeDocumentType(String documentType) {
        if (!StringUtils.hasText(documentType)) {
            return null;
        }
        return documentType.trim().toUpperCase(Locale.ROOT);
    }

    private record AiAnswer(String answer, boolean fallbackUsed) {
    }
}
