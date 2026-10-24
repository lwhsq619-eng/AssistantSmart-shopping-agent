package com.example.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.dto.OrderSaveDTO;
import com.example.mall.entity.Orders;
import com.example.mall.vo.OrderVO;

public interface OrdersService extends IService<Orders> {
    IPage<OrderVO> pageOrders(OrderQueryDTO query);
    void createOrder(OrderSaveDTO dto);
    void updateOrder(Long id, OrderSaveDTO dto);
    void updateStatus(Long id, String status);
    OrderVO getOrderVO(Long id);
}
