package com.example.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.dto.UserQueryDTO;
import com.example.mall.dto.UserSaveDTO;
import com.example.mall.entity.SysUser;
import com.example.mall.vo.LoginVO;

public interface SysUserService extends IService<SysUser> {
    SysUser login(LoginDTO dto);
    LoginVO adminLogin(LoginDTO dto);
    LoginVO userLogin(LoginDTO dto);
    void register(RegisterDTO dto);
    IPage<SysUser> pageUsers(UserQueryDTO query);
    void createUser(UserSaveDTO dto);
    void updateUser(Long id, UserSaveDTO dto);
    void updateStatus(Long id, Integer status);
}
