package com.example.mall.service.impl;

import com.example.mall.exception.BusinessException;
import com.example.mall.service.CaptchaService;
import com.example.mall.vo.CaptchaVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int EXPIRE_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, CaptchaEntry> cache = new ConcurrentHashMap<>();

    @Override
    public CaptchaVO generate() {
        clearExpired();
        String captchaId = UUID.randomUUID().toString();
        String code = randomCode();
        cache.put(captchaId, new CaptchaEntry(code, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));
        return new CaptchaVO(captchaId, drawImage(code));
    }

    @Override
    public void validateAndRemove(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(400, "请输入验证码");
        }
        CaptchaEntry entry = cache.remove(captchaId);
        if (entry == null || entry.expireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "验证码已过期，请刷新后重试");
        }
        if (!entry.code().equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(400, "验证码错误，请刷新后重试");
        }
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    private String drawImage(String code) {
        int width = 130;
        int height = 44;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(244, 248, 255));
        g.fillRoundRect(0, 0, width, height, 12, 12);

        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(80 + RANDOM.nextInt(120), 110 + RANDOM.nextInt(100), 160 + RANDOM.nextInt(80), 140));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + RANDOM.nextInt(80), 70 + RANDOM.nextInt(90), 150 + RANDOM.nextInt(80)));
            int rotate = RANDOM.nextInt(25) - 12;
            g.rotate(Math.toRadians(rotate), 26 + i * 24, 28);
            g.drawString(String.valueOf(code.charAt(i)), 18 + i * 26, 31);
            g.rotate(Math.toRadians(-rotate), 26 + i * 24, 28);
        }
        g.dispose();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("验证码生成失败");
        }
    }

    private void clearExpired() {
        LocalDateTime now = LocalDateTime.now();
        cache.entrySet().removeIf(entry -> entry.getValue().expireAt().isBefore(now));
    }

    private record CaptchaEntry(String code, LocalDateTime expireAt) {
    }
}
