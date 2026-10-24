package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.dto.AddressDTO;
import com.example.mall.entity.UserAddress;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.UserAddressMapper;
import com.example.mall.utils.AuthContext;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final UserAddressMapper addressMapper;

    public AddressController(UserAddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @GetMapping
    public Result<List<UserAddress>> list() {
        return Result.success("查询成功", addressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, AuthContext.userId())
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime)));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody AddressDTO dto) {
        Long userId = AuthContext.userId();
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(userId);
        }
        LocalDateTime now = LocalDateTime.now();
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);
        address.setCreateTime(now);
        address.setUpdateTime(now);
        addressMapper.insert(address);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        UserAddress address = requireOwn(id);
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(address.getUserId());
        }
        BeanUtils.copyProperties(dto, address);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        requireOwn(id);
        addressMapper.deleteById(id);
        return Result.success();
    }

    private UserAddress requireOwn(Long id) {
        UserAddress address = addressMapper.selectById(id);
        if (address == null || !AuthContext.userId().equals(address.getUserId())) {
            throw new BusinessException("收货地址不存在");
        }
        return address;
    }

    private void clearDefault(Long userId) {
        List<UserAddress> addresses = addressMapper.selectList(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
        for (UserAddress address : addresses) {
            address.setIsDefault(0);
            addressMapper.updateById(address);
        }
    }
}
