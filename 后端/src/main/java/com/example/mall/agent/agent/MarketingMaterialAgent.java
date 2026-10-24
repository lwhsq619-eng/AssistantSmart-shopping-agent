package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.MarketingMaterialItem;
import com.example.mall.agent.dto.agent.MarketingMaterialRequest;
import com.example.mall.agent.dto.agent.MarketingMaterialResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.MarketingMaterialPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.ProductTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 商家运营营销素材生成 Agent。
 * 只生成运营审核建议，不发布内容，不修改商品和活动数据。
 */
@Service
public class MarketingMaterialAgent {
    private static final Logger log = LoggerFactory.getLogger(MarketingMaterialAgent.class);
    private static final int MAX_BATCH_SIZE = 20;
    private static final Set<String> ALLOWED_CHANNELS = Set.of(
            "COMMUNITY",
            "LIVE",
            "SHORT_VIDEO",
            "POSTER",
            "PUSH",
            "SLOGAN"
    );

    private final ProductTool productTool;
    private final AiService aiService;
    private final MarketingMaterialPrompt marketingMaterialPrompt;
    private final ObjectMapper objectMapper;

    public MarketingMaterialAgent(ProductTool productTool,
                                  AiService aiService,
                                  MarketingMaterialPrompt marketingMaterialPrompt,
                                  ObjectMapper objectMapper) {
        this.productTool = productTool;
        this.aiService = aiService;
        this.marketingMaterialPrompt = marketingMaterialPrompt;
        this.objectMapper = objectMapper;
    }

    public MarketingMaterialResponse generate(MarketingMaterialRequest request) {
        String activityName = resolveActivityName(request == null ? null : request.getActivityName());
        String validationError = validateRequest(request);
        if (validationError != null) {
            return MarketingMaterialResponse.fail(activityName, validationError);
        }

        String style = StringUtils.hasText(request.getStyle()) ? request.getStyle().trim() : "简洁专业";
        List<String> channels = normalizeChannels(request.getChannels());
        long start = System.currentTimeMillis();
        List<MarketingMaterialItem> materials = new ArrayList<>();

        for (Long productId : request.getProductIds()) {
            materials.add(generateSingle(productId, activityName, request.getTargetAudience(), style, channels, request.getExtraRequirement()));
        }

        log.info("MarketingMaterialAgent success, productCount={}, activityName={}, elapsedMs={}",
                request.getProductIds().size(), activityName, System.currentTimeMillis() - start);
        return MarketingMaterialResponse.builder()
                .success(true)
                .activityName(activityName)
                .materials(materials)
                .message("生成完成，素材仅作为运营审核建议，未自动发布。")
                .build();
    }

    private MarketingMaterialItem generateSingle(Long productId,
                                                 String activityName,
                                                 String targetAudience,
                                                 String style,
                                                 List<String> channels,
                                                 String extraRequirement) {
        try {
            ProductToolResponse product = productTool.getProductDetail(productId);
            try {
                String prompt = buildPrompt(product, activityName, targetAudience, style, channels, extraRequirement);
                AiChatResponse aiResponse = useReasoner(channels, extraRequirement)
                        ? aiService.reason(marketingMaterialPrompt.systemPrompt(), prompt)
                        : aiService.chat(marketingMaterialPrompt.systemPrompt(), prompt);
                if (Boolean.TRUE.equals(aiResponse.getSuccess()) && StringUtils.hasText(aiResponse.getContent())) {
                    MarketingMaterialItem item = parseAiResult(aiResponse.getContent(), product, channels);
                    return sanitizeResult(item, product, activityName, channels);
                }
                return fallbackResult(product, activityName, channels, null);
            } catch (Exception e) {
                log.warn("MarketingMaterialAgent AI fallback, productId={}, error={}", productId, e.getMessage());
                return fallbackResult(product, activityName, channels, "AI 调用失败，已生成基础模板素材。");
            }
        } catch (Exception e) {
            log.warn("MarketingMaterialAgent product failed, productId={}, error={}", productId, e.getMessage());
            return MarketingMaterialItem.builder()
                    .success(false)
                    .productId(productId)
                    .errorMessage("营销素材生成失败：" + e.getMessage())
                    .build();
        }
    }

    private String buildPrompt(ProductToolResponse product,
                               String activityName,
                               String targetAudience,
                               String style,
                               List<String> channels,
                               String extraRequirement) {
        return "请只返回 JSON，格式：{\"productId\":"
                + product.getProductId()
                + ",\"slogan\":\"\",\"communityPost\":\"\",\"liveScript\":\"\",\"shortVideoScript\":\"\","
                + "\"posterText\":\"\",\"pushMessage\":\"\"}\n"
                + "活动名称：" + activityName + "\n"
                + "目标人群：" + safe(targetAudience) + "\n"
                + "文案风格：" + style + "\n"
                + "生成渠道：" + channels + "\n"
                + "额外要求：" + safe(extraRequirement) + "\n"
                + "商品真实数据："
                + "商品ID=" + product.getProductId()
                + "，商品名称=" + product.getProductName()
                + "，分类=" + product.getCategoryName()
                + "，价格=" + product.getPrice()
                + "，库存=" + product.getStock()
                + "，销量=" + product.getSales()
                + "，描述=" + safe(product.getDescription())
                + "，优惠信息=" + safe(product.getDiscountInfo())
                + "\n要求：活动标语 10~20 字；社群文案 80~150 字；直播脚本 30~60 秒；短视频脚本含镜头、旁白、字幕；"
                + "海报文案含主标题、副标题、按钮文案；推送消息不超过 40 字；不能虚构折扣、赠品、库存。";
    }

    private MarketingMaterialItem parseAiResult(String content, ProductToolResponse product, List<String> channels) throws Exception {
        JsonNode node = objectMapper.readTree(extractJson(content));
        return MarketingMaterialItem.builder()
                .success(true)
                .productId(product.getProductId())
                .productName(product.getProductName())
                .slogan(hasChannel(channels, "SLOGAN") ? textOrEmpty(node, "slogan") : null)
                .communityPost(hasChannel(channels, "COMMUNITY") ? textOrEmpty(node, "communityPost") : null)
                .liveScript(hasChannel(channels, "LIVE") ? textOrEmpty(node, "liveScript") : null)
                .shortVideoScript(hasChannel(channels, "SHORT_VIDEO") ? textOrEmpty(node, "shortVideoScript") : null)
                .posterText(hasChannel(channels, "POSTER") ? textOrEmpty(node, "posterText") : null)
                .pushMessage(hasChannel(channels, "PUSH") ? textOrEmpty(node, "pushMessage") : null)
                .build();
    }

    private MarketingMaterialItem sanitizeResult(MarketingMaterialItem item,
                                                 ProductToolResponse product,
                                                 String activityName,
                                                 List<String> channels) {
        MarketingMaterialItem fallback = fallbackResult(product, activityName, channels, null);
        return MarketingMaterialItem.builder()
                .success(true)
                .productId(product.getProductId())
                .productName(product.getProductName())
                .slogan(textOrFallback(item.getSlogan(), fallback.getSlogan()))
                .communityPost(textOrFallback(item.getCommunityPost(), fallback.getCommunityPost()))
                .liveScript(textOrFallback(item.getLiveScript(), fallback.getLiveScript()))
                .shortVideoScript(textOrFallback(item.getShortVideoScript(), fallback.getShortVideoScript()))
                .posterText(textOrFallback(item.getPosterText(), fallback.getPosterText()))
                .pushMessage(trimPushMessage(textOrFallback(item.getPushMessage(), fallback.getPushMessage())))
                .build();
    }

    private MarketingMaterialItem fallbackResult(ProductToolResponse product,
                                                 String activityName,
                                                 List<String> channels,
                                                 String warning) {
        return MarketingMaterialItem.builder()
                .success(true)
                .productId(product.getProductId())
                .productName(product.getProductName())
                .slogan(hasChannel(channels, "SLOGAN") ? buildFallbackSlogan(product, activityName) : null)
                .communityPost(hasChannel(channels, "COMMUNITY")
                        ? "这款商品适合关注实用性和性价比的用户，欢迎进店了解详情。" : null)
                .liveScript(hasChannel(channels, "LIVE")
                        ? "大家可以看看这款商品，价格、库存和详情以页面显示为准。" : null)
                .shortVideoScript(hasChannel(channels, "SHORT_VIDEO")
                        ? "镜头展示商品外观和使用场景，字幕突出商品名称和基础卖点。" : null)
                .posterText(hasChannel(channels, "POSTER") ? "精选好物，欢迎选购。" : null)
                .pushMessage(hasChannel(channels, "PUSH") ? "你关注的商品有新活动，快来看看。" : null)
                .errorMessage(warning)
                .build();
    }

    private String buildFallbackSlogan(ProductToolResponse product, String activityName) {
        String slogan = product.getProductName() + " " + activityName;
        return slogan.length() > 20 ? slogan.substring(0, 20) : slogan;
    }

    private String validateRequest(MarketingMaterialRequest request) {
        if (request == null || request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return "productIds 不能为空。";
        }
        if (request.getProductIds().size() > MAX_BATCH_SIZE) {
            return "productIds 单次最多支持 20 个。";
        }
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            return "channels 不能为空。";
        }
        if (normalizeChannels(request.getChannels()).isEmpty()) {
            return "channels 不合法。";
        }
        return null;
    }

    private List<String> normalizeChannels(List<String> channels) {
        Set<String> result = new LinkedHashSet<>();
        for (String channel : channels) {
            if (!StringUtils.hasText(channel)) {
                continue;
            }
            String normalized = channel.trim().toUpperCase();
            if (ALLOWED_CHANNELS.contains(normalized)) {
                result.add(normalized);
            }
        }
        return new ArrayList<>(result);
    }

    private boolean useReasoner(List<String> channels, String extraRequirement) {
        return channels.size() >= 4 || (StringUtils.hasText(extraRequirement) && extraRequirement.length() >= 20);
    }

    private boolean hasChannel(List<String> channels, String channel) {
        return channels.contains(channel);
    }

    private String resolveActivityName(String activityName) {
        return StringUtils.hasText(activityName) ? activityName.trim() : "商城活动";
    }

    private String textOrFallback(String value, String fallback) {
        String sanitized = sanitizeText(value);
        return StringUtils.hasText(sanitized) ? sanitized : fallback;
    }

    private String sanitizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.replace("全网最低", "高性价比")
                .replace("绝对第一", "表现出色")
                .replace("永久有效", "以页面展示为准")
                .replace("100%有效", "效果因实际使用而异")
                .replace("买就送", "活动详情以页面展示为准")
                .replace("限时赠品", "活动信息以页面展示为准");
    }

    private String trimPushMessage(String message) {
        if (message == null || message.length() <= 40) {
            return message;
        }
        return message.substring(0, 40);
    }

    private String extractJson(String content) {
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String textOrEmpty(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText("");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
