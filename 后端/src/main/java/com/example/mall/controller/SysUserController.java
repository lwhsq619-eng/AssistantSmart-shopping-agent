package com.example.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mall.common.Result;
import com.example.mall.dto.UserQueryDTO;
import com.example.mall.dto.UserSaveDTO;
import com.example.mall.entity.SysUser;
import com.example.mall.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users")
public class SysUserController {
    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public Result<IPage<SysUser>> page(@Valid UserQueryDTO query) {
        return Result.success("查询成功", userService.pageUsers(query));
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list(@RequestParam(required = false) Integer status) {
        List<SysUser> users = userService.lambdaQuery()
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .list();
        return Result.success("查询成功", users);
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.success("查询成功", userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserSaveDTO dto) {
        userService.createUser(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveDTO dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
