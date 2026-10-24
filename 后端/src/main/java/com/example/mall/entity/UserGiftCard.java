package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_gift_card")
public class UserGiftCard {
    @TableId
    private Long id;
    private Long userId;
    private Long giftCardId;
    private String giftCardName;
    private BigDecimal amount;
    private String recipient;
    private String blessing;
    private String redeemCode;
    private String status;
    private LocalDateTime createTime;
}
