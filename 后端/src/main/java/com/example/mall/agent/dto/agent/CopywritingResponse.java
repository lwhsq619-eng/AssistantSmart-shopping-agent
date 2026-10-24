package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 商品文案批量生成响应 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CopywritingResponse {
    private Boolean success;
    private Integer total;
    private Integer successCount;
    private Integer failureCount;
    private List<CopywritingItemResult> results;
    private String message;

    public static CopywritingResponse fail(String message) {
        return CopywritingResponse.builder()
                .success(false)
                .total(0)
                .successCount(0)
                .failureCount(0)
                .message(message)
                .results(List.of())
                .build();
    }
}
