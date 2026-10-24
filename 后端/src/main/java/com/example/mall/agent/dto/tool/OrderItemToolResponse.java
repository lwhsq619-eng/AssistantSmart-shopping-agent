package com.example.mall.agent.dto.tool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品明细 Tool DTO。
 */
@Data
@Builder
public class OrderItemToolResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private String specInfo;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
