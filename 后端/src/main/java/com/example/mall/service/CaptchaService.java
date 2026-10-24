package com.example.mall.service;

import com.example.mall.vo.CaptchaVO;

public interface CaptchaService {
    CaptchaVO generate();

    void validateAndRemove(String captchaId, String captchaCode);
}
