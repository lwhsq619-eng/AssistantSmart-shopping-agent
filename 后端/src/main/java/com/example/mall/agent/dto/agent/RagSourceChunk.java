package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 知识片段 DTO，兼作文档存储和问答来源返回。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RagSourceChunk {
    private String documentId;
    private String documentType;
    private String chunkId;
    private Integer chunkIndex;
    private Double score;
    private String content;
    private List<String> keywords;
    private String createTime;
}
