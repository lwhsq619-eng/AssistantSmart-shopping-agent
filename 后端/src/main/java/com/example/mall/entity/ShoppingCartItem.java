package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shopping_cart_item")
public class ShoppingCartItem {
    @TableId
    private Long id;
    private Long cartId;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private String specInfo;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
