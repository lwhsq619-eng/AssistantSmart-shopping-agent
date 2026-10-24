package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * RAG 文档上传响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RagDocumentUploadResponse {
    private Boolean success;
    private String documentId;
    private Integer chunkCount;
    private String message;

    public static RagDocumentUploadResponse fail(String message) {
        return RagDocumentUploadResponse.builder()
                .success(false)
                .chunkCount(0)
                .message(message)
                .build();
    }
}
