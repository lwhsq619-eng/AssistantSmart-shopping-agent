package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("gift_card")
public class GiftCard {
    @TableId
    private Long id;
    private String name;
    private BigDecimal amount;
    private String imageUrl;
    private String description;
    private Integer status;
}
