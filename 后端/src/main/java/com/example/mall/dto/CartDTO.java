package com.example.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartDTO {
    @NotNull(message = "商品不能为空")
    private Long productId;
    private String specInfo;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
    private Integer selected = 1;
}
