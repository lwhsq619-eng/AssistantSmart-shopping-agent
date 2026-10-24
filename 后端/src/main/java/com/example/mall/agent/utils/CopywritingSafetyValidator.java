package com.example.mall.agent.utils;

import com.example.mall.agent.dto.agent.CopywritingContent;
import com.example.mall.agent.dto.agent.CopywritingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 第七阶段文案安全校验器。
 * 只做文本层面的二次清洗，不修改商品数据。
 */
@Component
public class CopywritingSafetyValidator {
    private static final Logger log = LoggerFactory.getLogger(CopywritingSafetyValidator.class);
    private static final List<String> PROMOTION_WORDS = List.of("满减", "折扣", "优惠券", "赠品", "到手价", "立减", "领券");

    public CopywritingContent sanitize(CopywritingContent content,
                                       Set<CopywritingType> requestedTypes,
                                       boolean hasDiscountInfo,
                                       CopywritingContent fallback) {
        CopywritingContent safeContent = content == null ? fallback : content;
        return CopywritingContent.builder()
                .title(requestedTypes.contains(CopywritingType.TITLE)
                        ? safeTitle(safeContent.getTitle(), fallback.getTitle(), hasDiscountInfo)
                        : null)
                .sellingPoints(requestedTypes.contains(CopywritingType.SELLING_POINTS)
                        ? safeSellingPoints(safeContent.getSellingPoints(), fallback.getSellingPoints(), hasDiscountInfo)
                        : null)
                .detail(requestedTypes.contains(CopywritingType.DETAIL)
                        ? safeTextOrFallback(safeContent.getDetail(), fallback.getDetail(), hasDiscountInfo)
                        : null)
                .promotion(requestedTypes.contains(CopywritingType.PROMOTION)
                        ? safeTextOrFallback(safeContent.getPromotion(), fallback.getPromotion(), hasDiscountInfo)
                        : null)
                .shortVideoScript(requestedTypes.contains(CopywritingType.SHORT_VIDEO_SCRIPT)
                        ? safeTextOrFallback(safeContent.getShortVideoScript(), fallback.getShortVideoScript(), hasDiscountInfo)
                        : null)
                .liveScript(requestedTypes.contains(CopywritingType.LIVE_SCRIPT)
                        ? safeTextOrFallback(safeContent.getLiveScript(), fallback.getLiveScript(), hasDiscountInfo)
                        : null)
                .build();
    }

    public CopywritingContent emptyContent(Set<CopywritingType> requestedTypes) {
        return CopywritingContent.builder()
                .title(requestedTypes.contains(CopywritingType.TITLE) ? "" : null)
                .sellingPoints(requestedTypes.contains(CopywritingType.SELLING_POINTS) ? List.of() : null)
                .detail(requestedTypes.contains(CopywritingType.DETAIL) ? "" : null)
                .promotion(requestedTypes.contains(CopywritingType.PROMOTION) ? "" : null)
                .shortVideoScript(requestedTypes.contains(CopywritingType.SHORT_VIDEO_SCRIPT) ? "" : null)
                .liveScript(requestedTypes.contains(CopywritingType.LIVE_SCRIPT) ? "" : null)
                .build();
    }

    public Set<CopywritingType> allTypes() {
        return EnumSet.allOf(CopywritingType.class);
    }

    private String safeTitle(String value, String fallback, boolean hasDiscountInfo) {
        String text = safeTextOrFallback(value, fallback, hasDiscountInfo);
        if (text != null && text.length() > 35) {
            return text.substring(0, 35);
        }
        return text;
    }

    private List<String> safeSellingPoints(List<String> values, List<String> fallback, boolean hasDiscountInfo) {
        List<String> source = values == null || values.isEmpty() ? fallback : values;
        if (source == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String value : source) {
            String text = safeText(value, hasDiscountInfo);
            if (StringUtils.hasText(text)) {
                result.add(text.length() > 30 ? text.substring(0, 30) : text);
            }
            if (result.size() >= 5) {
                break;
            }
        }
        return result;
    }

    private String safeTextOrFallback(String value, String fallback, boolean hasDiscountInfo) {
        String text = safeText(value, hasDiscountInfo);
        if (StringUtils.hasText(text)) {
            return text;
        }
        return safeText(fallback, hasDiscountInfo);
    }

    private String safeText(String value, boolean hasDiscountInfo) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String result = value;
        result = replaceAbsoluteWords(result);
        if (!hasDiscountInfo) {
            result = removePromotionWords(result);
        }
        return result.trim();
    }

    private String replaceAbsoluteWords(String text) {
        String result = text;
        result = replaceWithLog(result, "全网最低", "高性价比");
        result = replaceWithLog(result, "全国第一", "表现出色");
        result = replaceWithLog(result, "世界第一", "表现出色");
        result = replaceWithLog(result, "绝对最好", "体验较好");
        result = replaceWithLog(result, "永久有效", "以页面展示为准");
        result = replaceWithLog(result, "100%有效", "实际效果以使用体验为准");
        result = replaceWithLog(result, "百分百", "较高");
        result = replaceWithLog(result, "零风险", "风险较低");
        result = replaceWithLog(result, "保证成功", "提升使用体验");
        result = replaceWithLog(result, "包治百病", "满足日常需求");
        result = replaceWithLog(result, "无任何副作用", "以实际体验为准");
        result = replaceWithLog(result, "唯一", "优选");
        result = replaceWithLog(result, "最低", "实惠");
        result = replaceWithLog(result, "顶级", "高品质");
        return result;
    }

    private String removePromotionWords(String text) {
        String result = text;
        for (String word : PROMOTION_WORDS) {
            if (result.contains(word)) {
                log.warn("Copywriting safety removed promotion word: {}", word);
                result = result.replace(word, "页面信息");
            }
        }
        return result;
    }

    private String replaceWithLog(String text, String source, String replacement) {
        if (text.contains(source)) {
            log.warn("Copywriting safety replaced absolute expression: {}", source);
            return text.replace(source, replacement);
        }
        return text;
    }
}
