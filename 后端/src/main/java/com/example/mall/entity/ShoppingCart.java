package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shopping_cart")
public class ShoppingCart {
    @TableId
    private Long id;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
