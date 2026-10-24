package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 单个商品生成的文案内容。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CopywritingContent {
    private String title;
    private List<String> sellingPoints;
    private String detail;
    private String promotion;
    private String shortVideoScript;
    private String liveScript;
}
