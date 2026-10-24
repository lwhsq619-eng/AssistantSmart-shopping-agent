package com.example.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutDTO {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    private List<Long> cartItemIds;
    private Long productId;
    private String specInfo;
    private Integer quantity;
    private String paymentMethod = "余额模拟支付";
}
