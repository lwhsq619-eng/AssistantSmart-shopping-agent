package com.example.mall.agent.rag;

import com.example.mall.agent.dto.agent.RagSourceChunk;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 文档切分器。
 * 默认每片约 700 字，片段间保留 100 字重叠，便于跨段检索。
 */
@Component
public class RagChunker {
    private static final int CHUNK_SIZE = 700;
    private static final int OVERLAP = 100;

    private final RagEmbeddingService embeddingService;

    public RagChunker(RagEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public List<RagSourceChunk> chunk(String documentId, String documentType, String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = text.replace("\r\n", "\n").trim();
        List<RagSourceChunk> chunks = new ArrayList<>();
        int start = 0;
        int index = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            String content = normalized.substring(start, end).trim();
            if (StringUtils.hasText(content)) {
                chunks.add(RagSourceChunk.builder()
                        .documentId(documentId)
                        .documentType(documentType)
                        .chunkId(documentId + "-chunk-" + String.format("%03d", index + 1))
                        .chunkIndex(index)
                        .score(0.0)
                        .content(content)
                        .keywords(embeddingService.extractKeywords(content))
                        .createTime(LocalDateTime.now().toString())
                        .build());
                index++;
            }
            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(end - OVERLAP, start + 1);
        }
        return chunks;
    }
}
