package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * 单个商品文案生成结果 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CopywritingItemResult {
    private Long productId;
    private String productName;
    private Boolean success;
    private CopywritingContent content;
    private Boolean fallback;
    private String errorMessage;
}
