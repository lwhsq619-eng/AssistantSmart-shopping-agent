package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 售后处理建议 DTO，只作为人工审核参考。
 */
@Data
@Builder
public class AfterSaleAdvice {
    private String handlerRole;
    private String suggestedAction;
    private Boolean needManualReview;
}
