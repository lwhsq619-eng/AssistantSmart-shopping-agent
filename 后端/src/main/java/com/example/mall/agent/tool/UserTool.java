package com.example.mall.agent.tool;

import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.entity.SysUser;
import com.example.mall.service.OrderItemService;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductService;
import com.example.mall.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户业务 Tool。
 * 当前项目没有浏览历史、收藏、优惠券、购物车独立 Service，因此相关字段返回空集合。
 */
@Component
public class UserTool {
    private static final Logger log = LoggerFactory.getLogger(UserTool.class);

    private final SysUserService userService;
    private final OrdersService ordersService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final OrderTool orderTool;

    public UserTool(SysUserService userService,
                    OrdersService ordersService,
                    OrderItemService orderItemService,
                    ProductService productService,
                    OrderTool orderTool) {
        this.userService = userService;
        this.ordersService = ordersService;
        this.orderItemService = orderItemService;
        this.productService = productService;
        this.orderTool = orderTool;
    }

    public UserToolResponse getUserProfile(Long userId) {
        long start = System.currentTimeMillis();
        SysUser user = requireUser(userId);
        UserToolResponse result = baseUserResponse(user, orderTool.getUserOrders(userId, 5), List.of(), List.of());
        log.info("Tool call success, tool=UserTool.getUserProfile, userId={}, elapsedMs={}, count=1",
                userId, System.currentTimeMillis() - start);
        return result;
    }

    public UserToolResponse getUserBehaviorSummary(Long userId) {
        long start = System.currentTimeMillis();
        SysUser user = requireUser(userId);
        List<OrderToolResponse> recentOrders = orderTool.getUserOrders(userId, 5);
        List<String> categories = collectPurchasedCategories(userId);
        UserToolResponse result = baseUserResponse(user, recentOrders, categories, categories);
        log.info("Tool call success, tool=UserTool.getUserBehaviorSummary, userId={}, elapsedMs={}, count={}",
                userId, System.currentTimeMillis() - start, categories.size());
        return result;
    }

    public List<String> getUserCouponList(Long userId) {
        long start = System.currentTimeMillis();
        requireUser(userId);
        log.info("Tool call success, tool=UserTool.getUserCouponList, userId={}, elapsedMs={}, count=0",
                userId, System.currentTimeMillis() - start);
        return List.of();
    }

    public List<String> getUserPreference(Long userId) {
        long start = System.currentTimeMillis();
        requireUser(userId);
        List<String> result = collectPurchasedCategories(userId);
        log.info("Tool call success, tool=UserTool.getUserPreference, userId={}, elapsedMs={}, count={}",
                userId, System.currentTimeMillis() - start, result.size());
        return result;
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new AiException(400, "用户ID不能为空");
        }
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new AiException(404, "用户不存在");
        }
        return user;
    }

    private UserToolResponse baseUserResponse(SysUser user,
                                              List<OrderToolResponse> recentOrders,
                                              List<String> historyCategories,
                                              List<String> preferenceTags) {
        List<OrderToolResponse> safeRecentOrders = recentOrders == null ? List.of() : recentOrders;
        return UserToolResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .level(resolveLevel(safeRecentOrders.size()))
                .historyCategories(historyCategories == null ? List.of() : historyCategories)
                .recentOrders(safeRecentOrders)
                .cartItems(List.of())
                .coupons(List.of())
                .preferenceTags(preferenceTags == null ? List.of() : preferenceTags)
                .build();
    }

    private List<String> collectPurchasedCategories(Long userId) {
        List<Orders> orders = ordersService.lambdaQuery()
                .eq(Orders::getUserId, userId)
                .list()
                .stream()
                .sorted(Comparator.comparing(Orders::getOrderTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .toList();
        Set<String> categories = new LinkedHashSet<>();
        for (Orders order : orders) {
            List<OrderItem> items = orderItemService.lambdaQuery().eq(OrderItem::getOrderId, order.getId()).list();
            for (OrderItem item : items) {
                if (item.getProductId() == null) {
                    continue;
                }
                Product product = productService.getById(item.getProductId());
                if (product != null && product.getCategoryName() != null) {
                    categories.add(product.getCategoryName());
                }
            }
        }
        return new ArrayList<>(categories);
    }

    private String resolveLevel(int recentOrderCount) {
        if (recentOrderCount >= 10) {
            return "VIP";
        }
        if (recentOrderCount >= 3) {
            return "活跃用户";
        }
        return "普通用户";
    }
}
