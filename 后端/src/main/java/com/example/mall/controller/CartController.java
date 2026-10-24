package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.dto.CartDTO;
import com.example.mall.entity.Product;
import com.example.mall.entity.ShoppingCart;
import com.example.mall.entity.ShoppingCartItem;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.ShoppingCartItemMapper;
import com.example.mall.mapper.ShoppingCartMapper;
import com.example.mall.service.ProductService;
import com.example.mall.utils.AuthContext;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final ShoppingCartMapper cartMapper;
    private final ShoppingCartItemMapper itemMapper;
    private final ProductService productService;

    public CartController(ShoppingCartMapper cartMapper, ShoppingCartItemMapper itemMapper, ProductService productService) {
        this.cartMapper = cartMapper;
        this.itemMapper = itemMapper;
        this.productService = productService;
    }

    @GetMapping
    public Result<List<ShoppingCartItem>> list() {
        Long userId = AuthContext.userId();
        return Result.success("查询成功", itemMapper.selectList(new LambdaQueryWrapper<ShoppingCartItem>()
                .eq(ShoppingCartItem::getUserId, userId)
                .orderByDesc(ShoppingCartItem::getUpdateTime)));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> add(@Valid @RequestBody CartDTO dto) {
        Long userId = AuthContext.userId();
        Product product = productService.getById(dto.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        ShoppingCart cart = getOrCreateCart(userId);
        String spec = dto.getSpecInfo() == null ? "默认规格" : dto.getSpecInfo();
        ShoppingCartItem existing = itemMapper.selectOne(new LambdaQueryWrapper<ShoppingCartItem>()
                .eq(ShoppingCartItem::getUserId, userId)
                .eq(ShoppingCartItem::getProductId, dto.getProductId())
                .eq(ShoppingCartItem::getSpecInfo, spec)
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            int quantity = existing.getQuantity() + dto.getQuantity();
            if (quantity > product.getStock()) {
                throw new BusinessException("购买数量不能超过库存");
            }
            existing.setQuantity(quantity);
            existing.setSelected(1);
            existing.setUpdateTime(now);
            itemMapper.updateById(existing);
            return Result.success();
        }
        if (dto.getQuantity() > product.getStock()) {
            throw new BusinessException("购买数量不能超过库存");
        }
        ShoppingCartItem item = new ShoppingCartItem();
        item.setCartId(cart.getId());
        item.setUserId(userId);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImageUrl());
        item.setSpecInfo(spec);
        item.setPrice(product.getDiscountPrice() == null ? product.getPrice() : product.getDiscountPrice());
        item.setQuantity(dto.getQuantity());
        item.setSelected(1);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        itemMapper.insert(item);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CartDTO dto) {
        Long userId = AuthContext.userId();
        ShoppingCartItem item = itemMapper.selectById(id);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new BusinessException("购物车商品不存在");
        }
        Product product = productService.getById(item.getProductId());
        if (dto.getQuantity() != null) {
            if (product != null && dto.getQuantity() > product.getStock()) {
                throw new BusinessException("购买数量不能超过库存");
            }
            item.setQuantity(dto.getQuantity());
        }
        if (dto.getSelected() != null) {
            item.setSelected(dto.getSelected());
        }
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ShoppingCartItem item = itemMapper.selectById(id);
        if (item == null || !AuthContext.userId().equals(item.getUserId())) {
            throw new BusinessException("购物车商品不存在");
        }
        itemMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batch(@RequestBody List<Long> ids) {
        Long userId = AuthContext.userId();
        for (Long id : ids) {
            ShoppingCartItem item = itemMapper.selectById(id);
            if (item != null && userId.equals(item.getUserId())) {
                itemMapper.deleteById(id);
            }
        }
        return Result.success();
    }

    private ShoppingCart getOrCreateCart(Long userId) {
        ShoppingCart cart = cartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId).last("limit 1"));
        if (cart != null) {
            return cart;
        }
        LocalDateTime now = LocalDateTime.now();
        cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setCreateTime(now);
        cart.setUpdateTime(now);
        cartMapper.insert(cart);
        return cart;
    }
}
