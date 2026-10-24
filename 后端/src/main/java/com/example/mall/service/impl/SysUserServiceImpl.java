package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.dto.UserQueryDTO;
import com.example.mall.dto.UserSaveDTO;
import com.example.mall.entity.SysUser;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.SysUserMapper;
import com.example.mall.service.CaptchaService;
import com.example.mall.service.SysUserService;
import com.example.mall.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String DEFAULT_AVATAR = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80";

    private final CaptchaService captchaService;

    public SysUserServiceImpl(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Override
    public SysUser login(LoginDTO dto) {
        LoginVO loginVO = adminLogin(dto);
        SysUser user = getById(loginVO.getId());
        user.setPassword(null);
        return user;
    }

    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        captchaService.validateAndRemove(dto.getCaptchaId(), dto.getCaptchaCode());
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .eq(SysUser::getPassword, dto.getPassword())
                .eq(SysUser::getRole, ROLE_ADMIN)
                .orderByAsc(SysUser::getId)
                .last("limit 1")
                .one();
        if (user == null) {
            throw new BusinessException(401, "管理员账号或密码错误");
        }
        assertEnabled(user);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO userLogin(LoginDTO dto) {
        captchaService.validateAndRemove(dto.getCaptchaId(), dto.getCaptchaCode());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPassword, dto.getPassword())
                .eq(SysUser::getRole, ROLE_USER)
                .and(w -> w.eq(SysUser::getUsername, dto.getUsername()).or().eq(SysUser::getPhone, dto.getUsername()));
        wrapper.orderByAsc(SysUser::getId).last("limit 1");
        SysUser user = getOne(wrapper);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或密码错误");
        }
        assertEnabled(user);
        return buildLoginVO(user);
    }

    @Override
    public void register(RegisterDTO dto) {
        captchaService.validateAndRemove(dto.getCaptchaId(), dto.getCaptchaCode());
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
        if (existsByUsername(dto.getUsername(), null)) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (existsByPhone(dto.getPhone(), null)) {
            throw new BusinessException(400, "手机号已被注册");
        }
        if (existsByEmail(dto.getEmail(), null)) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setRole(ROLE_USER);
        user.setStatus(1);
        user.setAvatar(DEFAULT_AVATAR);
        user.setAvatarUrl(DEFAULT_AVATAR);
        user.setRegisterTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        save(user);
    }

    @Override
    public IPage<SysUser> pageUsers(UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> page = page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        page.getRecords().forEach(user -> user.setPassword(null));
        return page;
    }

    @Override
    public void createUser(UserSaveDTO dto) {
        if (existsByUsername(dto.getUsername(), null)) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(dto.getPhone()) && existsByPhone(dto.getPhone(), null)) {
            throw new BusinessException("手机号已存在");
        }
        if (StringUtils.hasText(dto.getEmail()) && existsByEmail(dto.getEmail(), null)) {
            throw new BusinessException("邮箱已存在");
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole(ROLE_USER);
        }
        if (!StringUtils.hasText(user.getAvatar())) {
            user.setAvatar(StringUtils.hasText(user.getAvatarUrl()) ? user.getAvatarUrl() : DEFAULT_AVATAR);
        }
        if (!StringUtils.hasText(user.getAvatarUrl())) {
            user.setAvatarUrl(user.getAvatar());
        }
        LocalDateTime now = LocalDateTime.now();
        user.setRegisterTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        save(user);
    }

    @Override
    public void updateUser(Long id, UserSaveDTO dto) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (existsByUsername(dto.getUsername(), id)) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(dto.getPhone()) && existsByPhone(dto.getPhone(), id)) {
            throw new BusinessException("手机号已存在");
        }
        if (StringUtils.hasText(dto.getEmail()) && existsByEmail(dto.getEmail(), id)) {
            throw new BusinessException("邮箱已存在");
        }
        BeanUtils.copyProperties(dto, user);
        user.setId(id);
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole(ROLE_USER);
        }
        if (!StringUtils.hasText(user.getAvatar())) {
            user.setAvatar(StringUtils.hasText(user.getAvatarUrl()) ? user.getAvatarUrl() : DEFAULT_AVATAR);
        }
        if (!StringUtils.hasText(user.getAvatarUrl())) {
            user.setAvatarUrl(user.getAvatar());
        }
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    private void assertEnabled(SysUser user) {
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
    }

    private LoginVO buildLoginVO(SysUser user) {
        String avatar = StringUtils.hasText(user.getAvatar()) ? user.getAvatar() : user.getAvatarUrl();
        return LoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .address(user.getAddress())
                .role(user.getRole())
                .avatar(avatar)
                .status(user.getStatus())
                .token(user.getRole() + "-" + user.getId() + "-" + System.currentTimeMillis())
                .build();
    }

    private boolean existsByUsername(String username, Long excludeId) {
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count() > 0;
    }

    private boolean existsByPhone(String phone, Long excludeId) {
        return lambdaQuery()
                .eq(SysUser::getPhone, phone)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count() > 0;
    }

    private boolean existsByEmail(String email, Long excludeId) {
        return lambdaQuery()
                .eq(SysUser::getEmail, email)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count() > 0;
    }
}
