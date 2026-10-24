package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("product_sku")
public class ProductSku {
    @TableId
    private Long id;
    private Long productId;
    private String skuName;
    private String specInfo;
    private BigDecimal price;
    private Integer stock;
}
