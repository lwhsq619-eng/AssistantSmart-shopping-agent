package com.example.mall.agent.dto.agent;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 语义化商品搜索请求 DTO。
 */
@Data
public class SemanticSearchRequest {
    private Long userId;

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Min(value = 1, message = "page 不能小于 1")
    private Integer page = 1;

    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 50, message = "size 不能大于 50")
    private Integer size = 10;
}
