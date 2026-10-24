package com.example.mall.agent.rag;

import com.example.mall.agent.dto.agent.RagSourceChunk;
import com.example.mall.agent.exception.AiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 本地向量存储。
 * 第一版使用 JSON 文件持久化，后续可替换为 Milvus、PGVector 等向量库。
 */
@Component
public class RagVectorStore {
    private static final Logger log = LoggerFactory.getLogger(RagVectorStore.class);
    private final ObjectMapper objectMapper;
    private final List<RagSourceChunk> chunks = new ArrayList<>();
    private final Path storePath = Paths.get(System.getProperty("user.dir"), "data", "rag-store.json");

    public RagVectorStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void load() {
        if (!Files.exists(storePath)) {
            return;
        }
        try {
            List<RagSourceChunk> loaded = objectMapper.readValue(storePath.toFile(), new TypeReference<>() {
            });
            chunks.clear();
            chunks.addAll(loaded == null ? List.of() : loaded);
            log.info("RagVectorStore loaded, path={}, count={}", storePath, chunks.size());
        } catch (IOException e) {
            throw new AiException(500, "读取 RAG 本地存储失败", e);
        }
    }

    public synchronized void saveDocument(String documentId, List<RagSourceChunk> newChunks) {
        chunks.removeIf(chunk -> documentId.equals(chunk.getDocumentId()));
        chunks.addAll(newChunks);
        persist();
    }

    public synchronized List<RagSourceChunk> allChunks() {
        return new ArrayList<>(chunks);
    }

    public synchronized boolean isEmpty() {
        return chunks.isEmpty();
    }

    private void persist() {
        try {
            Files.createDirectories(storePath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile(), chunks);
            log.info("RagVectorStore persisted, path={}, count={}", storePath, chunks.size());
        } catch (IOException e) {
            throw new AiException(500, "保存 RAG 本地存储失败", e);
        }
    }
}
