package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId
    private Long id;
    private String productNo;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String imageUrl;
    private String detailImageUrl;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private BigDecimal discountPrice;
    private Integer stock;
    private Integer sales;
    private BigDecimal rating;
    private Integer reviewCount;
    private String description;
    private String specs;
    private Integer status;
    private Integer isSpecial;
    private Integer isRecommended;
    private Integer isNew;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
