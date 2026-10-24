package com.example.mall.agent.controller;

import com.example.mall.agent.agent.RagKnowledgeAgent;
import com.example.mall.agent.dto.agent.RagDocumentUploadResponse;
import com.example.mall.agent.dto.agent.RagKnowledgeRequest;
import com.example.mall.agent.dto.agent.RagKnowledgeResponse;
import com.example.mall.agent.dto.agent.RagSourceChunk;
import com.example.mall.agent.exception.AiException;
import com.example.mall.agent.rag.RagChunker;
import com.example.mall.agent.rag.RagDocumentLoader;
import com.example.mall.agent.rag.RagVectorStore;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * RAG 知识库接口。
 * Controller 负责上传和问答入口，不直接调用 DeepSeek。
 */
@RestController
@RequestMapping("/api/ai/rag")
public class RagKnowledgeController {
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "PRODUCT_MANUAL",
            "AFTER_SALE_POLICY",
            "RETURN_RULE",
            "PROMOTION_RULE",
            "PLATFORM_HELP",
            "OTHER"
    );

    private final RagKnowledgeAgent ragKnowledgeAgent;
    private final RagDocumentLoader documentLoader;
    private final RagChunker chunker;
    private final RagVectorStore vectorStore;

    public RagKnowledgeController(RagKnowledgeAgent ragKnowledgeAgent,
                                  RagDocumentLoader documentLoader,
                                  RagChunker chunker,
                                  RagVectorStore vectorStore) {
        this.ragKnowledgeAgent = ragKnowledgeAgent;
        this.documentLoader = documentLoader;
        this.chunker = chunker;
        this.vectorStore = vectorStore;
    }

    @PostMapping(value = "/document/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RagDocumentUploadResponse upload(@RequestPart("file") MultipartFile file,
                                            @RequestParam("documentType") String documentType) {
        try {
            String safeType = normalizeDocumentType(documentType);
            String text = documentLoader.loadText(file);
            String documentId = "doc-" + UUID.randomUUID();
            List<RagSourceChunk> chunks = chunker.chunk(documentId, safeType, text);
            if (chunks.isEmpty()) {
                throw new AiException(400, "文档切分后无有效片段");
            }
            vectorStore.saveDocument(documentId, chunks);
            return RagDocumentUploadResponse.builder()
                    .success(true)
                    .documentId(documentId)
                    .chunkCount(chunks.size())
                    .message("上传成功")
                    .build();
        } catch (Exception e) {
            return RagDocumentUploadResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/ask")
    public RagKnowledgeResponse ask(@RequestBody RagKnowledgeRequest request) {
        return ragKnowledgeAgent.ask(request);
    }

    private String normalizeDocumentType(String documentType) {
        if (!StringUtils.hasText(documentType)) {
            throw new AiException(400, "documentType 不能为空");
        }
        String safeType = documentType.trim().toUpperCase(Locale.ROOT);
        if (!DOCUMENT_TYPES.contains(safeType)) {
            throw new AiException(400, "documentType 非法");
        }
        return safeType;
    }
}
