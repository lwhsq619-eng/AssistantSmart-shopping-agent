package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Orders;
import com.example.mall.entity.PaymentRecord;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.PaymentRecordMapper;
import com.example.mall.service.OrdersService;
import com.example.mall.utils.AuthContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final OrdersService ordersService;
    private final PaymentRecordMapper paymentRecordMapper;

    public PaymentController(OrdersService ordersService, PaymentRecordMapper paymentRecordMapper) {
        this.ordersService = ordersService;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @GetMapping("/{orderNo}")
    public Result<Map<String, Object>> get(@PathVariable String orderNo) {
        Orders order = requireOwnOrder(orderNo);
        PaymentRecord record = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderNo, orderNo)
                .orderByDesc(PaymentRecord::getCreateTime)
                .last("limit 1"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.put("payment", record);
        return Result.success("查询成功", data);
    }

    @PostMapping("/{orderNo}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> pay(@PathVariable String orderNo, @RequestParam(defaultValue = "余额模拟支付") String paymentMethod) {
        Orders order = requireOwnOrder(orderNo);
        if (!"待支付".equals(order.getStatus())) {
            throw new BusinessException("订单不可重复支付");
        }
        LocalDateTime now = LocalDateTime.now();
        PaymentRecord record = new PaymentRecord();
        record.setOrderNo(order.getOrderNo());
        record.setUserId(order.getUserId());
        record.setAmount(order.getPayAmount() == null ? order.getTotalAmount() : order.getPayAmount());
        record.setPaymentMethod(paymentMethod);
        record.setStatus("支付成功");
        record.setPayTime(now);
        record.setCreateTime(now);
        paymentRecordMapper.insert(record);

        order.setStatus("待发货");
        order.setPaymentMethod(paymentMethod);
        order.setPaidTime(now);
        order.setUpdateTime(now);
        ordersService.updateById(order);
        return Result.success();
    }

    private Orders requireOwnOrder(String orderNo) {
        Orders order = ordersService.lambdaQuery().eq(Orders::getOrderNo, orderNo).last("limit 1").one();
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!AuthContext.userId().equals(order.getUserId())) {
            throw new BusinessException(403, "只能操作自己的订单");
        }
        return order;
    }
}
