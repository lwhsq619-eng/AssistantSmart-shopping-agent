package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.dto.OrderSaveDTO;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.entity.SysUser;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.OrdersMapper;
import com.example.mall.service.OrderItemService;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductService;
import com.example.mall.service.SysUserService;
import com.example.mall.utils.OrderNoUtil;
import com.example.mall.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    private static final List<String> STATUS_LIST = List.of("待支付", "待发货", "待收货", "已完成", "已取消");

    private final OrderItemService orderItemService;
    private final SysUserService userService;
    private final ProductService productService;

    public OrdersServiceImpl(OrderItemService orderItemService, SysUserService userService, ProductService productService) {
        this.orderItemService = orderItemService;
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    public IPage<OrderVO> pageOrders(OrderQueryDTO query) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .like(StringUtils.hasText(query.getOrderNo()), Orders::getOrderNo, query.getOrderNo())
                .like(StringUtils.hasText(query.getUserName()), Orders::getUserName, query.getUserName())
                .eq(StringUtils.hasText(query.getStatus()), Orders::getStatus, query.getStatus())
                .orderByDesc(Orders::getOrderTime);
        IPage<Orders> orderPage = page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        Page<OrderVO> voPage = new Page<>(query.getCurrent(), query.getSize(), orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderSaveDTO dto) {
        validateStatus(dto.getStatus());
        SysUser user = requireUser(dto.getUserId());
        Product product = requireProduct(dto.getProductId());
        ensureStock(product, dto.getQuantity());

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        LocalDateTime now = LocalDateTime.now();

        Orders order = new Orders();
        order.setOrderNo(OrderNoUtil.next());
        order.setUserId(user.getId());
        order.setUserName(user.getUsername());
        order.setTotalAmount(subtotal);
        order.setPayAmount(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setShippingAddress(StringUtils.hasText(dto.getShippingAddress()) ? dto.getShippingAddress() : user.getAddress());
        order.setStatus(dto.getStatus());
        order.setOrderTime(now);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        save(order);

        orderItemService.save(buildItem(order.getId(), product, dto.getQuantity(), subtotal, now));
        product.setStock(product.getStock() - dto.getQuantity());
        product.setUpdateTime(now);
        productService.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(Long id, OrderSaveDTO dto) {
        validateStatus(dto.getStatus());
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        List<OrderItem> oldItems = orderItemService.lambdaQuery().eq(OrderItem::getOrderId, id).list();
        for (OrderItem oldItem : oldItems) {
            Product oldProduct = productService.getById(oldItem.getProductId());
            if (oldProduct != null) {
                oldProduct.setStock(oldProduct.getStock() + oldItem.getQuantity());
                productService.updateById(oldProduct);
            }
        }
        if (!oldItems.isEmpty()) {
            orderItemService.lambdaUpdate().eq(OrderItem::getOrderId, id).remove();
        }

        SysUser user = requireUser(dto.getUserId());
        Product product = requireProduct(dto.getProductId());
        ensureStock(product, dto.getQuantity());
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        LocalDateTime now = LocalDateTime.now();

        order.setUserId(user.getId());
        order.setUserName(user.getUsername());
        order.setTotalAmount(subtotal);
        order.setPayAmount(subtotal);
        order.setShippingAddress(StringUtils.hasText(dto.getShippingAddress()) ? dto.getShippingAddress() : user.getAddress());
        order.setStatus(dto.getStatus());
        order.setUpdateTime(now);
        updateById(order);

        orderItemService.save(buildItem(id, product, dto.getQuantity(), subtotal, now));
        product.setStock(product.getStock() - dto.getQuantity());
        product.setUpdateTime(now);
        productService.updateById(product);
    }

    @Override
    public void updateStatus(Long id, String status) {
        validateStatus(status);
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public OrderVO getOrderVO(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order);
    }

    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        List<OrderItem> items = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId)
                .list();
        if (!items.isEmpty()) {
            OrderItem first = items.get(0);
            vo.setProductId(first.getProductId());
            vo.setProductName(items.size() == 1 ? first.getProductName() : first.getProductName() + " 等 " + items.size() + " 件商品");
            vo.setProductImage(first.getProductImage());
            vo.setQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
            vo.setUnitPrice(first.getUnitPrice());
            vo.setSubtotal(items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return vo;
    }

    private OrderItem buildItem(Long orderId, Product product, Integer quantity, BigDecimal subtotal, LocalDateTime now) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImageUrl());
        item.setSpecInfo("默认规格");
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setSubtotal(subtotal);
        item.setReviewed(0);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        return item;
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private Product requireProduct(Long productId) {
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException("商品已下架");
        }
        return product;
    }

    private void ensureStock(Product product, Integer quantity) {
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
    }

    private void validateStatus(String status) {
        if (!STATUS_LIST.contains(status)) {
            throw new BusinessException("订单状态不合法");
        }
    }
}
