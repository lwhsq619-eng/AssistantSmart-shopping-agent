package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 Tool 返回 DTO。
 */
@Data
@Builder
public class OrderToolResponse {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String status;
    private String statusText;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private List<OrderItemToolResponse> items;
    private Boolean refundAvailable;
    private String logisticsInfo;
}
