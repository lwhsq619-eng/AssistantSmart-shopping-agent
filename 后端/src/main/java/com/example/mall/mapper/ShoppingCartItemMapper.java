package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.ShoppingCartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartItemMapper extends BaseMapper<ShoppingCartItem> {
}
