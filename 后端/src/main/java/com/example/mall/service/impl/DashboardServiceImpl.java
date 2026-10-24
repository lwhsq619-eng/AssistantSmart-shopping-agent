package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.service.DashboardService;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductCategoryService;
import com.example.mall.service.ProductService;
import com.example.mall.service.SysUserService;
import com.example.mall.vo.DashboardVO;
import com.example.mall.vo.OrderVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final SysUserService userService;
    private final OrdersService ordersService;

    public DashboardServiceImpl(ProductService productService, ProductCategoryService categoryService,
                                SysUserService userService, OrdersService ordersService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.ordersService = ordersService;
    }

    @Override
    public DashboardVO getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Orders> todayOrders = ordersService.list(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, start)
                .lt(Orders::getOrderTime, end));
        BigDecimal todaySales = todayOrders.stream()
                .filter(order -> !"已取消".equals(order.getStatus()))
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            long count = ordersService.count(new LambdaQueryWrapper<Orders>()
                    .ge(Orders::getOrderTime, dayStart)
                    .lt(Orders::getOrderTime, dayEnd));
            trend.add(Map.of("date", date.toString(), "count", count));
        }

        Map<String, Long> categoryCount = productService.list().stream()
                .collect(Collectors.groupingBy(Product::getCategoryName, LinkedHashMap::new, Collectors.counting()));
        List<Map<String, Object>> categoryRatio = categoryCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .toList();

        List<OrderVO> latestOrders = ordersService.lambdaQuery()
                .orderByDesc(Orders::getOrderTime)
                .last("limit 6")
                .list()
                .stream()
                .map(order -> ordersService.getOrderVO(order.getId()))
                .toList();

        List<Map<String, Object>> lowStock = productService.lambdaQuery()
                .lt(Product::getStock, 10)
                .orderByAsc(Product::getStock)
                .last("limit 6")
                .list()
                .stream()
                .map(product -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", product.getId());
                    map.put("name", product.getName());
                    map.put("stock", product.getStock());
                    map.put("imageUrl", product.getImageUrl());
                    return map;
                })
                .toList();

        return DashboardVO.builder()
                .productTotal(productService.count())
                .categoryTotal(categoryService.count())
                .userTotal(userService.count())
                .orderTotal(ordersService.count())
                .todayOrderTotal((long) todayOrders.size())
                .todaySales(todaySales)
                .lastSevenDaysOrders(trend)
                .categoryRatio(categoryRatio)
                .latestOrders(latestOrders)
                .lowStockProducts(lowStock)
                .build();
    }
}
