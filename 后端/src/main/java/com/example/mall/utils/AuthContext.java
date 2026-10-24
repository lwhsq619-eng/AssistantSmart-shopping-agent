package com.example.mall.utils;

import com.example.mall.exception.BusinessException;

public final class AuthContext {
    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        AuthUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public static Long userId() {
        return get().userId();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record AuthUser(Long userId, String role) {
    }
}
