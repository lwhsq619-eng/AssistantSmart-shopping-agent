package com.example.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderSaveDTO {
    @NotNull(message = "用户不能为空")
    private Long userId;
    @NotNull(message = "商品不能为空")
    private Long productId;
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;
    private String shippingAddress;
    private String status = "待付款";
}
