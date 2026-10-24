package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String address;
    private Integer status;
    private String role;
    private String avatar;
    private String avatarUrl;
    private LocalDateTime registerTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
