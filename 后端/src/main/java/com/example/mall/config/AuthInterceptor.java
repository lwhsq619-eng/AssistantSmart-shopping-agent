package com.example.mall.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.mall.common.Result;
import com.example.mall.utils.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;

    public AuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }

        AuthContext.AuthUser authUser = parseUser(request.getHeader("Authorization"));
        if (authUser == null) {
            writeJson(response, Result.fail(401, "请先登录"));
            return false;
        }
        AuthContext.set(authUser);
        String role = authUser.role();
        if ("ADMIN".equals(role)) {
            return true;
        }
        if ("USER".equals(role) && isUserAllowed(request)) {
            return true;
        }

        writeJson(response, Result.fail(403, "权限不足，无法访问该功能"));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private AuthContext.AuthUser parseUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        String[] parts = token.split("-");
        if (parts.length < 2) {
            return null;
        }
        if (!"ADMIN".equals(parts[0]) && !"USER".equals(parts[0])) {
            return null;
        }
        try {
            return new AuthContext.AuthUser(Long.parseLong(parts[1]), parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isUserAllowed(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if (uri.startsWith("/api/cart")
                || uri.startsWith("/api/addresses")
                || uri.startsWith("/api/orders")
                || uri.startsWith("/api/payments")
                || uri.startsWith("/api/reviews")
                || uri.startsWith("/api/gift-cards")) {
            return true;
        }
        return "GET".equalsIgnoreCase(method)
                && (uri.equals("/api/products")
                || uri.startsWith("/api/products/search")
                || uri.startsWith("/api/products/special")
                || uri.startsWith("/api/products/recommend")
                || uri.startsWith("/api/products/similar")
                || uri.startsWith("/api/products/")
                || uri.equals("/api/categories")
                || uri.startsWith("/api/categories/list")
                || uri.startsWith("/api/categories/")
                || uri.startsWith("/api/brands"));
    }

    private void writeJson(HttpServletResponse response, Result<Void> result) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
