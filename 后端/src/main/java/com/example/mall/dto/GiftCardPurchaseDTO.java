package com.example.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GiftCardPurchaseDTO {
    @NotNull(message = "礼品卡不能为空")
    private Long giftCardId;
    private String recipient;
    private String blessing;
    private String paymentMethod = "余额模拟支付";
}
