package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.CopywritingContent;
import com.example.mall.agent.dto.agent.CopywritingItemResult;
import com.example.mall.agent.dto.agent.CopywritingRequest;
import com.example.mall.agent.dto.agent.CopywritingResponse;
import com.example.mall.agent.dto.agent.CopywritingType;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.MerchantCopywritingPrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.utils.CopywritingSafetyValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 商家运营商品文案生成 Agent。
 * 只生成运营建议，不写回数据库，不自动覆盖商品信息。
 */
@Service
public class MerchantCopywritingAgent {
    private static final Logger log = LoggerFactory.getLogger(MerchantCopywritingAgent.class);
    private static final int MAX_BATCH_SIZE = 20;

    private final ProductTool productTool;
    private final DeepSeekService deepSeekService;
    private final MerchantCopywritingPrompt merchantCopywritingPrompt;
    private final CopywritingSafetyValidator safetyValidator;
    private final ObjectMapper objectMapper;

    public MerchantCopywritingAgent(ProductTool productTool,
                                    DeepSeekService deepSeekService,
                                    MerchantCopywritingPrompt merchantCopywritingPrompt,
                                    CopywritingSafetyValidator safetyValidator,
                                    ObjectMapper objectMapper) {
        this.productTool = productTool;
        this.deepSeekService = deepSeekService;
        this.merchantCopywritingPrompt = merchantCopywritingPrompt;
        this.safetyValidator = safetyValidator;
        this.objectMapper = objectMapper;
    }

    public CopywritingResponse generate(CopywritingRequest request) {
        long start = System.currentTimeMillis();
        String validationError = validateRequest(request);
        if (validationError != null) {
            return CopywritingResponse.fail(validationError);
        }

        List<Long> productIds = distinctProductIds(request.getProductIds());
        Set<CopywritingType> generateTypes = parseGenerateTypes(request.getGenerateTypes());
        String style = StringUtils.hasText(request.getStyle()) ? request.getStyle().trim() : "简洁专业";
        List<CopywritingItemResult> results = new ArrayList<>();

        for (Long productId : productIds) {
            results.add(generateSingle(productId, request, style, generateTypes));
        }

        int successCount = (int) results.stream().filter(item -> Boolean.TRUE.equals(item.getSuccess())).count();
        int failureCount = results.size() - successCount;
        boolean success = successCount > 0 && failureCount < results.size();
        if (results.isEmpty()) {
            success = false;
        }

        log.info("MerchantCopywritingAgent done, requestCount={}, distinctCount={}, generateTypes={}, style={}, successCount={}, failureCount={}, elapsedMs={}",
                request.getProductIds().size(), productIds.size(), generateTypes, style, successCount, failureCount,
                System.currentTimeMillis() - start);

        return CopywritingResponse.builder()
                .success(success)
                .total(productIds.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .message(success ? "生成完成，结果仅作为运营建议，未修改商品数据。" : "全部商品文案生成失败。")
                .build();
    }

    private CopywritingItemResult generateSingle(Long productId,
                                                 CopywritingRequest request,
                                                 String style,
                                                 Set<CopywritingType> generateTypes) {
        long start = System.currentTimeMillis();
        try {
            ProductToolResponse product = productTool.getProductDetail(productId);
            boolean fallback = false;
            CopywritingContent content;
            try {
                String prompt = buildPrompt(product, request, style, generateTypes);
                AiChatResponse response = deepSeekService.chat(merchantCopywritingPrompt.systemPrompt(), prompt);
                if (!Boolean.TRUE.equals(response.getSuccess()) || !StringUtils.hasText(response.getContent())) {
                    throw new IllegalStateException("DeepSeek 返回为空");
                }
                content = parseContent(response.getContent(), generateTypes);
            } catch (Exception e) {
                fallback = true;
                log.warn("MerchantCopywritingAgent single fallback, productId={}, error={}", productId, e.getMessage());
                content = fallbackContent(product, generateTypes);
            }
            CopywritingContent safeContent = safetyValidator.sanitize(
                    content,
                    generateTypes,
                    hasDiscountInfo(product),
                    fallbackContent(product, generateTypes)
            );
            log.info("MerchantCopywritingAgent item success, productId={}, fallback={}, elapsedMs={}",
                    productId, fallback, System.currentTimeMillis() - start);
            return CopywritingItemResult.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .success(true)
                    .content(safeContent)
                    .fallback(fallback)
                    .build();
        } catch (Exception e) {
            log.warn("MerchantCopywritingAgent item failed, productId={}, error={}", productId, e.getMessage());
            return CopywritingItemResult.builder()
                    .productId(productId)
                    .productName(null)
                    .success(false)
                    .content(safetyValidator.emptyContent(generateTypes))
                    .fallback(true)
                    .errorMessage("商品不存在或文案生成失败")
                    .build();
        }
    }

    private String buildPrompt(ProductToolResponse product,
                               CopywritingRequest request,
                               String style,
                               Set<CopywritingType> generateTypes) {
        return """
                请只返回 JSON：{"title":"","sellingPoints":[],"detail":"","promotion":"","shortVideoScript":"","liveScript":""}
                生成类型：%s
                文案风格：%s
                目标人群：%s
                使用场景：%s
                额外要求：%s
                商品真实数据：
                {
                  "productId": %s,
                  "productName": "%s",
                  "categoryName": "%s",
                  "price": %s,
                  "stock": %s,
                  "sales": %s,
                  "description": "%s",
                  "imageUrl": "%s",
                  "discountInfo": "%s"
                }
                未请求生成的字段必须返回 null。不得编造商品参数、优惠、折扣、赠品、库存或功效。
                """.formatted(generateTypes, style, safe(request.getTargetAudience()), safe(request.getScene()),
                safe(request.getExtraRequirement()), product.getProductId(), safe(product.getProductName()),
                safe(product.getCategoryName()), product.getPrice(), product.getStock(), product.getSales(),
                safe(product.getDescription()), safe(product.getImageUrl()), safe(product.getDiscountInfo()));
    }

    private CopywritingContent parseContent(String content, Set<CopywritingType> generateTypes) throws Exception {
        JsonNode node = objectMapper.readTree(extractJson(content));
        return CopywritingContent.builder()
                .title(generateTypes.contains(CopywritingType.TITLE) ? textOrNull(node, "title") : null)
                .sellingPoints(generateTypes.contains(CopywritingType.SELLING_POINTS) ? stringArray(node, "sellingPoints") : null)
                .detail(generateTypes.contains(CopywritingType.DETAIL) ? textOrNull(node, "detail") : null)
                .promotion(generateTypes.contains(CopywritingType.PROMOTION) ? textOrNull(node, "promotion") : null)
                .shortVideoScript(generateTypes.contains(CopywritingType.SHORT_VIDEO_SCRIPT) ? textOrNull(node, "shortVideoScript") : null)
                .liveScript(generateTypes.contains(CopywritingType.LIVE_SCRIPT) ? textOrNull(node, "liveScript") : null)
                .build();
    }

    private CopywritingContent fallbackContent(ProductToolResponse product, Set<CopywritingType> generateTypes) {
        return CopywritingContent.builder()
                .title(generateTypes.contains(CopywritingType.TITLE) ? fallbackTitle(product) : null)
                .sellingPoints(generateTypes.contains(CopywritingType.SELLING_POINTS) ? fallbackSellingPoints(product) : null)
                .detail(generateTypes.contains(CopywritingType.DETAIL) ? fallbackDetail(product) : null)
                .promotion(generateTypes.contains(CopywritingType.PROMOTION) ? fallbackPromotion() : null)
                .shortVideoScript(generateTypes.contains(CopywritingType.SHORT_VIDEO_SCRIPT) ? fallbackShortVideoScript(product) : null)
                .liveScript(generateTypes.contains(CopywritingType.LIVE_SCRIPT) ? fallbackLiveScript(product) : null)
                .build();
    }

    private String fallbackTitle(ProductToolResponse product) {
        String category = StringUtils.hasText(product.getCategoryName()) ? product.getCategoryName() : "日常场景";
        String title = safe(product.getProductName()) + "，适合" + category + "使用";
        return title.length() > 35 ? title.substring(0, 35) : title;
    }

    private List<String> fallbackSellingPoints(ProductToolResponse product) {
        List<String> points = new ArrayList<>();
        if (StringUtils.hasText(product.getProductName()) || StringUtils.hasText(product.getCategoryName())) {
            points.add("商品名称和分类信息清晰");
        }
        if (product.getPrice() != null) {
            points.add("页面展示价格为 " + product.getPrice());
        }
        if (product.getStock() != null) {
            points.add("当前库存以页面显示为准");
        }
        if (StringUtils.hasText(product.getDescription())) {
            String description = product.getDescription();
            points.add(description.length() > 30 ? description.substring(0, 30) : description);
        }
        return points.isEmpty() ? List.of("真实商品信息展示，适合日常选购") : points.stream().limit(5).toList();
    }

    private String fallbackDetail(ProductToolResponse product) {
        return "商品名称：" + safe(product.getProductName())
                + "\n商品分类：" + safe(product.getCategoryName())
                + "\n商品介绍：" + safe(product.getDescription())
                + "\n当前价格和库存请以商品页面实际展示为准。";
    }

    private String fallbackPromotion() {
        return "这款商品适合关注实用性和性价比的用户，具体价格、库存和活动信息请以商城页面为准。";
    }

    private String fallbackShortVideoScript(ProductToolResponse product) {
        return "镜头展示商品外观和页面信息，字幕突出商品名称和真实描述，引导用户进入商品详情页查看。";
    }

    private String fallbackLiveScript(ProductToolResponse product) {
        return "下面为大家介绍这款商品。商品名称、价格、库存和具体参数以商城页面展示为准，感兴趣的用户可以查看详情。";
    }

    private String validateRequest(CopywritingRequest request) {
        if (request == null || request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return "productIds 不能为空。";
        }
        if (request.getProductIds().size() > MAX_BATCH_SIZE) {
            return "productIds 单次最多支持 20 个商品。";
        }
        if (request.getProductIds().stream().anyMatch(id -> id == null)) {
            return "productIds 不能包含 null。";
        }
        if (request.getGenerateTypes() == null || request.getGenerateTypes().isEmpty()) {
            return "generateTypes 不能为空。";
        }
        Set<CopywritingType> types = parseGenerateTypes(request.getGenerateTypes());
        if (types.size() != request.getGenerateTypes().stream().filter(StringUtils::hasText).count()) {
            return "generateTypes 包含非法值。";
        }
        return null;
    }

    private Set<CopywritingType> parseGenerateTypes(List<String> generateTypes) {
        Set<CopywritingType> result = EnumSet.noneOf(CopywritingType.class);
        if (generateTypes == null) {
            return result;
        }
        for (String type : generateTypes) {
            CopywritingType.parse(type).ifPresent(result::add);
        }
        return result;
    }

    private List<Long> distinctProductIds(List<Long> productIds) {
        return new ArrayList<>(new LinkedHashSet<>(productIds));
    }

    private String extractJson(String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("DeepSeek 返回内容为空");
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json", "")
                    .replaceFirst("^```JSON", "")
                    .replaceFirst("^```", "")
                    .replaceFirst("```$", "")
                    .trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new IllegalArgumentException("无法提取 JSON");
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text : null;
    }

    private List<String> stringArray(JsonNode node, String field) {
        JsonNode array = node.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : array) {
            String value = item.asText();
            if (StringUtils.hasText(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private boolean hasDiscountInfo(ProductToolResponse product) {
        if (!StringUtils.hasText(product.getDiscountInfo())) {
            return false;
        }
        String discountInfo = product.getDiscountInfo();
        return !discountInfo.contains("暂无") && !discountInfo.equalsIgnoreCase("null");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
