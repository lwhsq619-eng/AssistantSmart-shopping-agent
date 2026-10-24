package com.example.mall.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 场景搭配组合 DTO。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SceneMatchGroup {
    private String groupName;
    private String reason;
    private BigDecimal totalPrice;
    private List<SceneProductItem> products;
}
