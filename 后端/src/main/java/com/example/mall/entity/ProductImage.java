package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_image")
public class ProductImage {
    @TableId
    private Long id;
    private Long productId;
    private String imageUrl;
    private Integer sortNo;
}
