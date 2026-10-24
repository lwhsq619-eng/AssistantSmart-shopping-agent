package com.example.mall.agent.rag;

import com.example.mall.agent.dto.agent.RagSourceChunk;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * RAG 检索服务。
 * 使用本地关键词相似度打分，最高分低于 0.2 时由 Agent 判定为无依据。
 */
@Service
public class RagSearchService {
    private static final Logger log = LoggerFactory.getLogger(RagSearchService.class);
    private static final double MIN_EVIDENCE_SCORE = 0.2;

    private final RagVectorStore vectorStore;
    private final RagEmbeddingService embeddingService;
    private final RagChunker chunker;
    private final RagDocumentLoader documentLoader;

    public RagSearchService(RagVectorStore vectorStore,
                            RagEmbeddingService embeddingService,
                            RagChunker chunker,
                            RagDocumentLoader documentLoader) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.chunker = chunker;
        this.documentLoader = documentLoader;
    }

    @PostConstruct
    public void seedDefaultDocuments() {
        if (!vectorStore.isEmpty()) {
            return;
        }
        saveDefaultDocument("doc-default-return-rule", "RETURN_RULE", "rag/default-return-rule.md");
        saveDefaultDocument("doc-default-after-sale-policy", "AFTER_SALE_POLICY", "rag/default-after-sale-policy.md");
    }

    public List<RagSourceChunk> search(String question, String documentType, int topK) {
        return vectorStore.allChunks().stream()
                .filter(chunk -> !StringUtils.hasText(documentType) || documentType.equals(chunk.getDocumentType()))
                .map(chunk -> withScore(chunk, embeddingService.score(question, chunk)))
                .filter(chunk -> chunk.getScore() != null && chunk.getScore() > 0)
                .sorted(Comparator.comparing(RagSourceChunk::getScore, Comparator.reverseOrder()))
                .limit(topK)
                .toList();
    }

    public boolean hasEvidence(List<RagSourceChunk> chunks) {
        return chunks != null && !chunks.isEmpty() && chunks.get(0).getScore() != null && chunks.get(0).getScore() >= MIN_EVIDENCE_SCORE;
    }

    private void saveDefaultDocument(String documentId, String documentType, String path) {
        String text = documentLoader.loadClasspathText(path);
        List<RagSourceChunk> chunks = chunker.chunk(documentId, documentType, text);
        vectorStore.saveDocument(documentId, chunks);
        log.info("RagSearchService seeded default document, documentId={}, type={}, chunkCount={}",
                documentId, documentType, chunks.size());
    }

    private RagSourceChunk withScore(RagSourceChunk source, double score) {
        return RagSourceChunk.builder()
                .documentId(source.getDocumentId())
                .documentType(source.getDocumentType())
                .chunkId(source.getChunkId())
                .chunkIndex(source.getChunkIndex())
                .score(score)
                .content(source.getContent())
                .keywords(source.getKeywords())
                .createTime(source.getCreateTime())
                .build();
    }
}
