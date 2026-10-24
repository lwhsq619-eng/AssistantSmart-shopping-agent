package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.service.CaptchaService;
import com.example.mall.vo.CaptchaVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping
    public Result<CaptchaVO> captcha() {
        return Result.success("验证码生成成功", captchaService.generate());
    }
}
