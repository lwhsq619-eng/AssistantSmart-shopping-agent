package com.example.mall.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String address;
    private String role;
    private String avatar;
    private Integer status;
    private String token;
}
