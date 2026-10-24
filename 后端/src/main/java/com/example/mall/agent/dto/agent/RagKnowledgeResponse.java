package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * RAG 知识库问答响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RagKnowledgeResponse {
    private Boolean success;
    private String answer;
    private List<RagSourceChunk> sources;
    private Boolean noEvidence;
    private Boolean fallbackUsed;
    private String message;

    public static RagKnowledgeResponse fail(String message) {
        return RagKnowledgeResponse.builder()
                .success(false)
                .answer("")
                .sources(List.of())
                .noEvidence(true)
                .message(message)
                .build();
    }
}
