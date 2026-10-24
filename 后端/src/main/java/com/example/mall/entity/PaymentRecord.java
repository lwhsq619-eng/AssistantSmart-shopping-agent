package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
