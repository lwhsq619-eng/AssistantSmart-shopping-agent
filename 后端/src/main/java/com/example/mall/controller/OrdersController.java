package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mall.common.Result;
import com.example.mall.dto.CheckoutDTO;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.dto.OrderSaveDTO;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.entity.ShoppingCartItem;
import com.example.mall.entity.UserAddress;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.PaymentRecordMapper;
import com.example.mall.mapper.ShoppingCartItemMapper;
import com.example.mall.mapper.UserAddressMapper;
import com.example.mall.service.OrderItemService;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductService;
import com.example.mall.utils.AuthContext;
import com.example.mall.utils.OrderNoUtil;
import com.example.mall.vo.OrderVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrdersService ordersService;
    private final OrderItemService orderItemService;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final ShoppingCartItemMapper cartItemMapper;
    private final UserAddressMapper addressMapper;
    private final ObjectMapper objectMapper;

    public OrdersController(OrdersService ordersService,
                            OrderItemService orderItemService,
                            OrderItemMapper orderItemMapper,
                            ProductService productService,
                            ShoppingCartItemMapper cartItemMapper,
                            UserAddressMapper addressMapper,
                            PaymentRecordMapper paymentRecordMapper,
                            ObjectMapper objectMapper) {
        this.ordersService = ordersService;
        this.orderItemService = orderItemService;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
        this.cartItemMapper = cartItemMapper;
        this.addressMapper = addressMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/page")
    public Result<IPage<OrderVO>> page(@Valid OrderQueryDTO query) {
        return Result.success("查询成功", ordersService.pageOrders(query));
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my(@RequestParam(required = false) String status) {
        Long userId = AuthContext.userId();
        List<Orders> orders = ordersService.lambdaQuery()
                .eq(Orders::getUserId, userId)
                .eq(status != null && !status.isBlank() && !"全部订单".equals(status), Orders::getStatus, status)
                .orderByDesc(Orders::getOrderTime)
                .list();
        return Result.success("查询成功", orders.stream().map(this::toDetail).toList());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        AuthContext.AuthUser user = AuthContext.get();
        if ("USER".equals(user.role()) && !user.userId().equals(order.getUserId())) {
            throw new BusinessException(403, "只能查看自己的订单");
        }
        return Result.success("查询成功", toDetail(order));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (body.containsKey("userId") && body.containsKey("productId")) {
            OrderSaveDTO dto = objectMapper.convertValue(body, OrderSaveDTO.class);
            ordersService.createOrder(dto);
            return Result.success("创建成功", Map.of());
        }
        CheckoutDTO dto = objectMapper.convertValue(body, CheckoutDTO.class);
        Orders order = createUserOrder(dto);
        return Result.success("提交订单成功", toDetail(order));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OrderSaveDTO dto) {
        ordersService.updateOrder(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Orders order = requireOwnOrder(id);
        if (!"待支付".equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单可以取消");
        }
        order.setStatus("已取消");
        order.setUpdateTime(LocalDateTime.now());
        ordersService.updateById(order);
        return Result.success();
    }

    @PutMapping("/{id}/confirm-receipt")
    public Result<Void> confirmReceipt(@PathVariable Long id) {
        Orders order = requireOwnOrder(id);
        if (!"待收货".equals(order.getStatus()) && !"待发货".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能确认收货");
        }
        order.setStatus("已完成");
        order.setUpdateTime(LocalDateTime.now());
        ordersService.updateById(order);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        ordersService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        ordersService.removeById(id);
        orderItemService.lambdaUpdate().eq(OrderItem::getOrderId, id).remove();
        return Result.success();
    }

    private Orders createUserOrder(CheckoutDTO dto) {
        Long userId = AuthContext.userId();
        UserAddress address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BusinessException("收货地址不存在");
        }
        List<OrderLine> lines = collectOrderLines(userId, dto);
        if (lines.isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }

        BigDecimal total = lines.stream().map(OrderLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = total.compareTo(new BigDecimal("300")) >= 0 ? new BigDecimal("10.00") : BigDecimal.ZERO;
        BigDecimal freight = total.compareTo(new BigDecimal("199")) >= 0 ? BigDecimal.ZERO : new BigDecimal("8.00");
        BigDecimal payAmount = total.subtract(discount).add(freight);
        LocalDateTime now = LocalDateTime.now();

        Orders order = new Orders();
        order.setOrderNo(OrderNoUtil.next());
        order.setUserId(userId);
        order.setUserName("用户" + userId);
        order.setTotalAmount(total);
        order.setDiscountAmount(discount);
        order.setFreightAmount(freight);
        order.setPayAmount(payAmount);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setShippingAddress(address.getReceiverName() + " " + address.getReceiverPhone() + " " + address.getProvince() + address.getCity() + address.getDetailAddress());
        order.setStatus("待支付");
        order.setOrderTime(now);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        ordersService.save(order);

        for (OrderLine line : lines) {
            Product product = line.product();
            if (product.getStock() == null || product.getStock() < line.quantity()) {
                throw new BusinessException(product.getName() + "库存不足");
            }
            product.setStock(product.getStock() - line.quantity());
            product.setSales((product.getSales() == null ? 0 : product.getSales()) + line.quantity());
            product.setUpdateTime(now);
            productService.updateById(product);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setSpecInfo(line.specInfo());
            item.setQuantity(line.quantity());
            item.setUnitPrice(line.price());
            item.setSubtotal(line.subtotal());
            item.setReviewed(0);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            orderItemMapper.insert(item);
        }
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            cartItemMapper.deleteBatchIds(dto.getCartItemIds());
        }
        return order;
    }

    private List<OrderLine> collectOrderLines(Long userId, CheckoutDTO dto) {
        List<OrderLine> lines = new ArrayList<>();
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            List<ShoppingCartItem> items = cartItemMapper.selectBatchIds(dto.getCartItemIds());
            for (ShoppingCartItem item : items) {
                if (!userId.equals(item.getUserId())) {
                    throw new BusinessException(403, "只能结算自己的购物车");
                }
                Product product = requireProduct(item.getProductId());
                lines.add(new OrderLine(product, item.getSpecInfo(), item.getPrice(), item.getQuantity(), item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
            }
        } else if (dto.getProductId() != null) {
            Product product = requireProduct(dto.getProductId());
            int quantity = dto.getQuantity() == null ? 1 : dto.getQuantity();
            BigDecimal price = product.getDiscountPrice() == null ? product.getPrice() : product.getDiscountPrice();
            lines.add(new OrderLine(product, dto.getSpecInfo(), price, quantity, price.multiply(BigDecimal.valueOf(quantity))));
        }
        return lines;
    }

    private Product requireProduct(Long productId) {
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        return product;
    }

    private Orders requireOwnOrder(Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        Long userId = AuthContext.userId();
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(403, "只能操作自己的订单");
        }
        return order;
    }

    private Map<String, Object> toDetail(Orders order) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.put("items", orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())));
        return data;
    }

    private record OrderLine(Product product, String specInfo, BigDecimal price, Integer quantity, BigDecimal subtotal) {
    }
}
