package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 内容安全审核响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentSafetyResponse {
    private Boolean success;
    private String contentType;
    private Boolean safe;
    private String riskLevel;
    private List<String> categories;
    private String suggestion;
    private String reason;
    private ContentSafetyResult javaResult;
    private ContentSafetyResult aiResult;
    private Boolean fallbackUsed;
    private String message;

    public static ContentSafetyResponse fail(String contentType, String message) {
        return ContentSafetyResponse.builder()
                .success(false)
                .contentType(contentType)
                .safe(false)
                .riskLevel("UNKNOWN")
                .categories(List.of())
                .suggestion("MANUAL_REVIEW")
                .reason(message)
                .message(message)
                .build();
    }
}
