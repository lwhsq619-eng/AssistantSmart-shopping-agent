package com.example.mall.agent.dto.agent;

import java.util.Locale;
import java.util.Optional;

/**
 * 第七阶段商品文案生成类型枚举，不污染原商城业务枚举。
 */
public enum CopywritingType {
    TITLE,
    SELLING_POINTS,
    DETAIL,
    PROMOTION,
    SHORT_VIDEO_SCRIPT,
    LIVE_SCRIPT;

    public static Optional<CopywritingType> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(CopywritingType.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
