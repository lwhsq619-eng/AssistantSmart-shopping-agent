package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.service.SysUserService;
import com.example.mall.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SysUserService userService;

    public AuthController(SysUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody LoginDTO dto) {
        return Result.success("管理员登录成功", userService.adminLogin(dto));
    }

    @PostMapping("/user/login")
    public Result<LoginVO> userLogin(@Valid @RequestBody LoginDTO dto) {
        return Result.success("用户登录成功", userService.userLogin(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("管理员登录成功", userService.adminLogin(dto));
    }
}
