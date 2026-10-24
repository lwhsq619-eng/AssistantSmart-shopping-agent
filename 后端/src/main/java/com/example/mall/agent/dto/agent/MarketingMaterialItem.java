package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * 单个商品营销素材结果 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketingMaterialItem {
    private Boolean success;
    private Long productId;
    private String productName;
    private String slogan;
    private String communityPost;
    private String liveScript;
    private String shortVideoScript;
    private String posterText;
    private String pushMessage;
    private String errorMessage;
}
