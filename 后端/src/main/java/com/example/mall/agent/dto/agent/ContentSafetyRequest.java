package com.example.mall.agent.dto.agent;

import lombok.Data;

/**
 * 内容安全审核请求 DTO。
 */
@Data
public class ContentSafetyRequest {
    private Long userId;
    private String contentType;
    private String content;
    private Long contextId;
}
