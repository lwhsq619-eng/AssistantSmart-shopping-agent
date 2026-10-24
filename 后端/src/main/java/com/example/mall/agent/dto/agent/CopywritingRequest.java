package com.example.mall.agent.dto.agent;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 商品文案批量生成请求 DTO。
 */
@Data
public class CopywritingRequest {
    @NotEmpty(message = "productIds 不能为空")
    @Size(max = 20, message = "productIds 单次最多支持 20 个商品")
    private List<Long> productIds;

    private String style;
    private String targetAudience;
    private String scene;

    @NotEmpty(message = "generateTypes 不能为空")
    private List<String> generateTypes;

    @Size(max = 500, message = "extraRequirement 长度不能超过 500")
    private String extraRequirement;
}
