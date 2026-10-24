package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_review")
public class ProductReview {
    @TableId
    private Long id;
    private Long productId;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private String username;
    private String avatar;
    private Integer rating;
    private String content;
    private String imageUrl;
    private Integer anonymous;
    private LocalDateTime createTime;
}
