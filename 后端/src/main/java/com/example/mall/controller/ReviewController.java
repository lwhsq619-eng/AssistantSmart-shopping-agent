package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.dto.ReviewDTO;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.entity.ProductReview;
import com.example.mall.entity.SysUser;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.ProductReviewMapper;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductService;
import com.example.mall.service.SysUserService;
import com.example.mall.utils.AuthContext;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ProductReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrdersService ordersService;
    private final ProductService productService;
    private final SysUserService userService;

    public ReviewController(ProductReviewMapper reviewMapper, OrderItemMapper orderItemMapper, OrdersService ordersService, ProductService productService, SysUserService userService) {
        this.reviewMapper = reviewMapper;
        this.orderItemMapper = orderItemMapper;
        this.ordersService = ordersService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/product/{productId}")
    public Result<List<ProductReview>> product(@PathVariable Long productId) {
        return Result.success("查询成功", reviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, productId)
                .orderByDesc(ProductReview::getCreateTime)));
    }

    @GetMapping("/my")
    public Result<List<ProductReview>> my() {
        return Result.success("查询成功", reviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getUserId, AuthContext.userId())
                .orderByDesc(ProductReview::getCreateTime)));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> create(@Valid @RequestBody ReviewDTO dto) {
        Long userId = AuthContext.userId();
        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        if (item == null) {
            throw new BusinessException("订单商品不存在");
        }
        Orders order = ordersService.getById(item.getOrderId());
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(403, "只能评价自己的订单");
        }
        if (!"已完成".equals(order.getStatus())) {
            throw new BusinessException("仅已完成订单可以评价");
        }
        if (Integer.valueOf(1).equals(item.getReviewed())) {
            throw new BusinessException("该商品已评价");
        }
        SysUser user = userService.getById(userId);
        ProductReview review = new ProductReview();
        review.setProductId(item.getProductId());
        review.setOrderId(order.getId());
        review.setOrderItemId(item.getId());
        review.setUserId(userId);
        review.setUsername(Integer.valueOf(1).equals(dto.getAnonymous()) ? "匿名用户" : user.getUsername());
        review.setAvatar(user.getAvatar());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImageUrl(dto.getImageUrl());
        review.setAnonymous(dto.getAnonymous());
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);

        item.setReviewed(1);
        orderItemMapper.updateById(item);
        refreshProductRating(item.getProductId());
        return Result.success();
    }

    private void refreshProductRating(Long productId) {
        List<ProductReview> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getProductId, productId));
        Product product = productService.getById(productId);
        if (product == null || reviews.isEmpty()) {
            return;
        }
        BigDecimal total = reviews.stream().map(r -> BigDecimal.valueOf(r.getRating())).reduce(BigDecimal.ZERO, BigDecimal::add);
        product.setReviewCount(reviews.size());
        product.setRating(total.divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP));
        productService.updateById(product);
    }
}
