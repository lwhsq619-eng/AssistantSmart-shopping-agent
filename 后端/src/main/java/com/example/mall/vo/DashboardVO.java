package com.example.mall.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardVO {
    private Long productTotal;
    private Long categoryTotal;
    private Long userTotal;
    private Long orderTotal;
    private Long todayOrderTotal;
    private BigDecimal todaySales;
    private List<Map<String, Object>> lastSevenDaysOrders;
    private List<Map<String, Object>> categoryRatio;
    private List<OrderVO> latestOrders;
    private List<Map<String, Object>> lowStockProducts;
}
