package com.example.mall.agent.rag;

import com.example.mall.agent.dto.agent.RagSourceChunk;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RAG 本地关键词向量服务。
 * DeepSeek 当前不强依赖 embedding，本服务用关键词和字符片段相似度保证项目可直接运行。
 */
@Service
public class RagEmbeddingService {
    private static final List<String> DOMAIN_TERMS = List.of(
            "七天无理由", "退货", "换货", "退款", "售后", "质量", "破损", "漏发", "错发",
            "包装", "定制商品", "虚拟商品", "二次销售", "优惠", "活动", "优惠券", "平台",
            "订单", "支付", "客服", "审核", "物流", "凭证", "商品", "规则", "政策"
    );

    public List<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = normalize(text);
        Set<String> keywords = new LinkedHashSet<>();
        for (String term : DOMAIN_TERMS) {
            if (normalized.contains(term.toLowerCase(Locale.ROOT))) {
                keywords.add(term);
            }
        }
        for (String token : normalized.split("[\\s,，。；;：:！!？?、（）()\\[\\]{}]+")) {
            if (token.length() >= 2 && token.length() <= 20) {
                keywords.add(token);
            }
        }
        String chineseOnly = normalized.replaceAll("[^\\u4e00-\\u9fa5A-Za-z0-9]", "");
        int maxNgram = Math.min(chineseOnly.length(), 80);
        for (int i = 0; i < maxNgram; i++) {
            if (i + 2 <= chineseOnly.length()) {
                keywords.add(chineseOnly.substring(i, i + 2));
            }
            if (i + 4 <= chineseOnly.length() && keywords.size() < 120) {
                keywords.add(chineseOnly.substring(i, i + 4));
            }
            if (keywords.size() >= 140) {
                break;
            }
        }
        return new ArrayList<>(keywords);
    }

    public double score(String question, RagSourceChunk chunk) {
        List<String> queryKeywords = extractKeywords(question);
        if (queryKeywords.isEmpty() || chunk == null || !StringUtils.hasText(chunk.getContent())) {
            return 0;
        }
        String content = normalize(chunk.getContent());
        Set<String> chunkKeywords = new LinkedHashSet<>(chunk.getKeywords() == null ? List.of() : chunk.getKeywords());
        int hit = 0;
        double weightedHit = 0;
        for (String keyword : queryKeywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }
            boolean matched = content.contains(keyword.toLowerCase(Locale.ROOT)) || chunkKeywords.contains(keyword);
            if (matched) {
                hit++;
                weightedHit += keyword.length() >= 4 ? 1.6 : 1.0;
            }
        }
        double keywordScore = weightedHit / Math.max(queryKeywords.size(), 1);
        double coverageScore = (double) hit / Math.max(queryKeywords.size(), 1);
        double exactBonus = content.contains(normalize(question)) ? 0.3 : 0;
        return Math.min(1.0, keywordScore * 0.7 + coverageScore * 0.3 + exactBonus);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
