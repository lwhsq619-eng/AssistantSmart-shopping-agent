package com.example.mall.agent.tool;

import com.example.mall.agent.dto.tool.OrderItemToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.service.OrderItemService;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * 订单业务 Tool。
 * 只调用已有 OrdersService、OrderItemService 和 ProductService，不直接访问 Mapper。
 */
@Component
public class OrderTool {
    private static final Logger log = LoggerFactory.getLogger(OrderTool.class);

    private final OrdersService ordersService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    public OrderTool(OrdersService ordersService, OrderItemService orderItemService, ProductService productService) {
        this.ordersService = ordersService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    public List<OrderToolResponse> getUserOrders(Long userId, Integer limit) {
        long start = System.currentTimeMillis();
        if (userId == null) {
            throw new AiException(400, "用户ID不能为空");
        }
        int safeLimit = normalizeLimit(limit);
        List<OrderToolResponse> result = ordersService.lambdaQuery()
                .eq(Orders::getUserId, userId)
                .list()
                .stream()
                .sorted(Comparator.comparing(Orders::getOrderTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(safeLimit)
                .map(this::toOrderResponse)
                .toList();
        log.info("Tool call success, tool=OrderTool.getUserOrders, userId={}, limit={}, elapsedMs={}, count={}",
                userId, safeLimit, System.currentTimeMillis() - start, result.size());
        return result;
    }

    public List<OrderToolResponse> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, Long categoryId) {
        long start = System.currentTimeMillis();
        if (startDate == null || endDate == null) {
            throw new AiException(400, "开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new AiException(400, "开始日期不能晚于结束日期");
        }
        List<OrderToolResponse> result = ordersService.lambdaQuery()
                .ge(Orders::getOrderTime, startDate.atStartOfDay())
                .le(Orders::getOrderTime, endDate.atTime(LocalTime.MAX))
                .list()
                .stream()
                .filter(order -> categoryId == null || orderContainsCategory(order.getId(), categoryId))
                .sorted(Comparator.comparing(Orders::getOrderTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toOrderResponse)
                .toList();
        log.info("Tool call success, tool=OrderTool.getOrdersByDateRange, startDate={}, endDate={}, categoryId={}, elapsedMs={}, count={}",
                startDate, endDate, categoryId, System.currentTimeMillis() - start, result.size());
        return result;
    }

    public OrderToolResponse getUserOrderDetail(Long userId, String orderNo) {
        long start = System.currentTimeMillis();
        Orders order = requireUserOrderByOrderNo(userId, orderNo);
        OrderToolResponse result = toOrderResponse(order);
        log.info("Tool call success, tool=OrderTool.getUserOrderDetail, userId={}, orderNo={}, elapsedMs={}, count=1",
                userId, orderNo, System.currentTimeMillis() - start);
        return result;
    }

    public OrderToolResponse getUserOrderStatus(Long userId, String orderNo) {
        long start = System.currentTimeMillis();
        Orders order = requireUserOrderByOrderNo(userId, orderNo);
        OrderToolResponse result = toOrderResponse(order);
        log.info("Tool call success, tool=OrderTool.getUserOrderStatus, userId={}, orderNo={}, elapsedMs={}, count=1",
                userId, orderNo, System.currentTimeMillis() - start);
        return result;
    }

    public OrderToolResponse getOrderDetail(Long orderId) {
        long start = System.currentTimeMillis();
        Orders order = requireOrder(orderId);
        OrderToolResponse result = toOrderResponse(order);
        log.info("Tool call success, tool=OrderTool.getOrderDetail, orderId={}, elapsedMs={}, count=1",
                orderId, System.currentTimeMillis() - start);
        return result;
    }

    public OrderToolResponse getOrderStatus(Long orderId) {
        long start = System.currentTimeMillis();
        Orders order = requireOrder(orderId);
        OrderToolResponse result = toOrderResponse(order);
        log.info("Tool call success, tool=OrderTool.getOrderStatus, orderId={}, elapsedMs={}, count=1",
                orderId, System.currentTimeMillis() - start);
        return result;
    }

    public Boolean checkRefundAvailable(Long orderId) {
        long start = System.currentTimeMillis();
        Orders order = requireOrder(orderId);
        Boolean result = isRefundAvailable(order);
        log.info("Tool call success, tool=OrderTool.checkRefundAvailable, orderId={}, elapsedMs={}, count=1",
                orderId, System.currentTimeMillis() - start);
        return result;
    }

    private Orders requireOrder(Long orderId) {
        if (orderId == null) {
            throw new AiException(400, "订单ID不能为空");
        }
        Orders order = ordersService.getById(orderId);
        if (order == null) {
            throw new AiException(404, "订单不存在");
        }
        return order;
    }

    private Orders requireUserOrderByOrderNo(Long userId, String orderNo) {
        if (userId == null) {
            throw new AiException(400, "用户ID不能为空");
        }
        if (orderNo == null || orderNo.isBlank()) {
            throw new AiException(400, "订单号不能为空");
        }
        return ordersService.lambdaQuery()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getOrderNo, orderNo)
                .list()
                .stream()
                .findFirst()
                .orElseThrow(() -> new AiException(404, "未查询到该用户下的订单信息"));
    }

    private boolean orderContainsCategory(Long orderId, Long categoryId) {
        if (orderId == null || categoryId == null) {
            return false;
        }
        List<OrderItem> items = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .list();
        for (OrderItem item : items) {
            if (item.getProductId() == null) {
                continue;
            }
            Product product = productService.getById(item.getProductId());
            if (product != null && categoryId.equals(product.getCategoryId())) {
                return true;
            }
        }
        return false;
    }

    private OrderToolResponse toOrderResponse(Orders order) {
        List<OrderItemToolResponse> items = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .list()
                .stream()
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toItemResponse)
                .toList();
        return OrderToolResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .status(order.getStatus())
                .statusText(order.getStatus())
                .totalAmount(order.getPayAmount() != null ? order.getPayAmount() : order.getTotalAmount())
                .createTime(order.getCreateTime())
                .payTime(order.getPaidTime())
                .deliveryTime(null)
                .items(items)
                .refundAvailable(isRefundAvailable(order))
                .logisticsInfo(buildLogisticsInfo(order))
                .build();
    }

    private OrderItemToolResponse toItemResponse(OrderItem item) {
        return OrderItemToolResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .specInfo(item.getSpecInfo())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private Boolean isRefundAvailable(Orders order) {
        return "待发货".equals(order.getStatus()) || "待收货".equals(order.getStatus());
    }

    private String buildLogisticsInfo(Orders order) {
        if ("待付款".equals(order.getStatus())) {
            return "订单尚未支付，暂无物流信息";
        }
        if ("待发货".equals(order.getStatus())) {
            return "商家正在备货，暂未发出";
        }
        if ("待收货".equals(order.getStatus())) {
            return "订单已发出，配送中";
        }
        if ("已完成".equals(order.getStatus())) {
            return "订单已签收完成";
        }
        return "暂无物流信息";
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }
}
