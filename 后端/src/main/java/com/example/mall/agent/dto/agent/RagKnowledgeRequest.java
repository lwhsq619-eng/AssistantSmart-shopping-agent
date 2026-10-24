package com.example.mall.agent.dto.agent;

import lombok.Data;

/**
 * RAG 知识库问答请求 DTO。
 */
@Data
public class RagKnowledgeRequest {
    private Long userId;
    private String question;
    private String documentType;
    private Integer topK;
}
