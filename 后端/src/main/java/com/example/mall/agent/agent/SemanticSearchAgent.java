package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.SearchIntentResult;
import com.example.mall.agent.dto.agent.SearchProductResult;
import com.example.mall.agent.dto.agent.SemanticSearchRequest;
import com.example.mall.agent.dto.agent.SemanticSearchResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.SemanticSearchPrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.ProductTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语义化商品搜索 Agent。
 * DeepSeek 只负责解析自然语言意图，商品检索、排序和解释全部基于 ProductTool 的真实数据。
 */
@Service
public class SemanticSearchAgent {
    private static final Logger log = LoggerFactory.getLogger(SemanticSearchAgent.class);
    private static final String SORT_RELEVANCE = "RELEVANCE";
    private static final String SORT_PRICE_ASC = "PRICE_ASC";
    private static final String SORT_PRICE_DESC = "PRICE_DESC";
    private static final String SORT_SALES_DESC = "SALES_DESC";

    private final ProductTool productTool;
    private final DeepSeekService deepSeekService;
    private final SemanticSearchPrompt semanticSearchPrompt;
    private final ObjectMapper objectMapper;

    public SemanticSearchAgent(ProductTool productTool,
                               DeepSeekService deepSeekService,
                               SemanticSearchPrompt semanticSearchPrompt,
                               ObjectMapper objectMapper) {
        this.productTool = productTool;
        this.deepSeekService = deepSeekService;
        this.semanticSearchPrompt = semanticSearchPrompt;
        this.objectMapper = objectMapper;
    }

    public SemanticSearchResponse search(SemanticSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getKeyword())) {
            return SemanticSearchResponse.fail(null, "搜索关键词不能为空。");
        }
        String query = request.getKeyword().trim();
        int page = normalizePage(request.getPage());
        int size = normalizeSize(request.getSize());
        long start = System.currentTimeMillis();

        try {
            SearchIntentResult intent = parseIntent(query);
            SearchResult searchResult = searchByIntent(intent, query, size);
            List<SearchProductResult> ranked = rankProducts(searchResult.products(), intent);
            List<SearchProductResult> pageRecords = paginate(ranked, page, size);

            String message = buildResponseMessage(ranked, searchResult.fallback());
            log.info("SemanticSearchAgent success, query={}, normalizedKeyword={}, aiParsed={}, fallback={}, elapsedMs={}, total={}",
                    query, intent.getNormalizedKeyword(), intent.getAiParsed(), searchResult.fallback(),
                    System.currentTimeMillis() - start, ranked.size());

            return SemanticSearchResponse.builder()
                    .success(true)
                    .query(query)
                    .normalizedKeyword(intent.getNormalizedKeyword())
                    .categoryName(intent.getCategoryName())
                    .minPrice(intent.getMinPrice())
                    .maxPrice(intent.getMaxPrice())
                    .sceneTags(safeList(intent.getSceneTags()))
                    .functionTags(safeList(intent.getFunctionTags()))
                    .tags(mergeTags(intent))
                    .sortBy(intent.getSortBy())
                    .products(pageRecords)
                    .fallback(searchResult.fallback())
                    .fallbackUsed(searchResult.fallback())
                    .message(message)
                    .page(page)
                    .size(size)
                    .total(ranked.size())
                    .build();
        } catch (Exception e) {
            log.error("SemanticSearchAgent failed, query={}", query, e);
            return SemanticSearchResponse.fail(query, "语义搜索失败：" + e.getMessage());
        }
    }

    private SearchIntentResult parseIntent(String query) {
        SearchIntentResult ruleIntent = buildRuleIntent(query, false);
        try {
            String prompt = buildIntentPrompt(query);
            AiChatResponse aiResponse = deepSeekService.chat(semanticSearchPrompt.systemPrompt(), prompt);
            if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
                return ruleIntent;
            }
            SearchIntentResult aiIntent = parseAiJson(aiResponse.getContent());
            aiIntent.setAiParsed(true);
            return mergeIntent(aiIntent, ruleIntent);
        } catch (Exception e) {
            log.warn("SemanticSearchAgent intent parse fallback, query={}, error={}", query, e.getMessage());
            return ruleIntent;
        }
    }

    private String buildIntentPrompt(String query) {
        return """
                请解析下面的搜索语句，只返回 JSON。
                搜索语句：%s
                可选分类：数码科技、手机通讯、电脑办公、家居生活、服饰鞋包、美妆护肤、食品饮料、运动户外、图书文具、母婴用品、宠物用品、礼品专区。
                sortBy 只能是 RELEVANCE、PRICE_ASC、PRICE_DESC、SALES_DESC。
                """.formatted(query);
    }

    private SearchIntentResult parseAiJson(String content) throws Exception {
        JsonNode node = objectMapper.readTree(extractJson(content));
        return SearchIntentResult.builder()
                .normalizedKeyword(cleanText(textOrNull(node, "normalizedKeyword")))
                .categoryName(normalizeCategory(textOrNull(node, "categoryName")))
                .minPrice(decimalOrNull(node, "minPrice"))
                .maxPrice(decimalOrNull(node, "maxPrice"))
                .sceneTags(arrayText(node, "sceneTags"))
                .functionTags(arrayText(node, "functionTags"))
                .excludedTags(arrayText(node, "excludedTags"))
                .sortBy(normalizeSortBy(textOrNull(node, "sortBy")))
                .aiParsed(true)
                .build();
    }

    private SearchIntentResult mergeIntent(SearchIntentResult aiIntent, SearchIntentResult ruleIntent) {
        return SearchIntentResult.builder()
                .normalizedKeyword(firstText(aiIntent.getNormalizedKeyword(), ruleIntent.getNormalizedKeyword()))
                .categoryName(firstText(aiIntent.getCategoryName(), ruleIntent.getCategoryName()))
                .minPrice(aiIntent.getMinPrice() != null ? aiIntent.getMinPrice() : ruleIntent.getMinPrice())
                .maxPrice(aiIntent.getMaxPrice() != null ? aiIntent.getMaxPrice() : ruleIntent.getMaxPrice())
                .sceneTags(mergeList(aiIntent.getSceneTags(), ruleIntent.getSceneTags()))
                .functionTags(mergeList(aiIntent.getFunctionTags(), ruleIntent.getFunctionTags()))
                .excludedTags(mergeList(aiIntent.getExcludedTags(), ruleIntent.getExcludedTags()))
                .sortBy(firstText(aiIntent.getSortBy(), ruleIntent.getSortBy()))
                .aiParsed(true)
                .build();
    }

    private SearchIntentResult buildRuleIntent(String query, boolean aiParsed) {
        return SearchIntentResult.builder()
                .normalizedKeyword(extractKeyword(query))
                .categoryName(extractCategory(query))
                .minPrice(extractMinPrice(query))
                .maxPrice(extractMaxPrice(query))
                .sceneTags(extractSceneTags(query))
                .functionTags(extractFunctionTags(query))
                .excludedTags(extractExcludedTags(query))
                .sortBy(inferSortBy(query))
                .aiParsed(aiParsed)
                .build();
    }

    private SearchResult searchByIntent(SearchIntentResult intent, String query, int size) {
        Map<Long, ProductToolResponse> collected = new LinkedHashMap<>();
        boolean fallback = !Boolean.TRUE.equals(intent.getAiParsed());

        collect(collected, productTool.searchProducts(
                firstText(intent.getNormalizedKeyword(), query),
                intent.getCategoryName(),
                intent.getMinPrice(),
                intent.getMaxPrice()));

        if (collected.isEmpty()) {
            fallback = true;
            for (String keyword : expandKeywords(query, intent)) {
                collect(collected, productTool.searchProducts(keyword, intent.getCategoryName(), intent.getMinPrice(), intent.getMaxPrice()));
                collect(collected, productTool.searchProducts(keyword, null, intent.getMinPrice(), intent.getMaxPrice()));
            }
        }

        if (collected.isEmpty()) {
            fallback = true;
            collect(collected, productTool.getHotProducts(Math.max(size, 10)));
        }

        List<ProductToolResponse> filtered = collected.values().stream()
                .filter(product -> !matchesExcludedTags(product, intent.getExcludedTags()))
                .toList();
        return new SearchResult(filtered, fallback);
    }

    private void collect(Map<Long, ProductToolResponse> target, List<ProductToolResponse> products) {
        if (products == null) {
            return;
        }
        for (ProductToolResponse product : products) {
            if (product != null && product.getProductId() != null) {
                target.putIfAbsent(product.getProductId(), product);
            }
        }
    }

    private List<SearchProductResult> rankProducts(List<ProductToolResponse> products, SearchIntentResult intent) {
        Comparator<SearchProductResult> comparator;
        switch (firstText(intent.getSortBy(), SORT_RELEVANCE)) {
            case SORT_PRICE_ASC -> comparator = this::comparePriceAsc;
            case SORT_PRICE_DESC -> comparator = this::comparePriceDesc;
            case SORT_SALES_DESC -> comparator = this::compareSalesDesc;
            default -> comparator = this::compareScoreDesc;
        }

        if (SORT_RELEVANCE.equals(firstText(intent.getSortBy(), SORT_RELEVANCE))) {
            comparator = comparator.thenComparing(this::compareSalesDesc).thenComparing(this::comparePriceAsc);
        } else {
            comparator = comparator.thenComparing(this::compareScoreDesc);
        }

        return products.stream()
                .map(product -> toSearchProductResult(product, intent))
                .sorted(comparator)
                .toList();
    }

    private int compareScoreDesc(SearchProductResult left, SearchProductResult right) {
        return Double.compare(nullToZero(right.getMatchScore()), nullToZero(left.getMatchScore()));
    }

    private int comparePriceAsc(SearchProductResult left, SearchProductResult right) {
        return compareNullableAsc(left.getPrice(), right.getPrice());
    }

    private int comparePriceDesc(SearchProductResult left, SearchProductResult right) {
        return compareNullableDesc(left.getPrice(), right.getPrice());
    }

    private int compareSalesDesc(SearchProductResult left, SearchProductResult right) {
        return compareNullableDesc(left.getSales(), right.getSales());
    }

    private <T extends Comparable<T>> int compareNullableAsc(T left, T right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private <T extends Comparable<T>> int compareNullableDesc(T left, T right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }

    private double nullToZero(Double value) {
        return value == null ? 0D : value;
    }

    private SearchProductResult toSearchProductResult(ProductToolResponse product, SearchIntentResult intent) {
        double score = scoreProduct(product, intent);
        return SearchProductResult.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .categoryName(product.getCategoryName())
                .price(product.getPrice())
                .stock(product.getStock())
                .sales(product.getSales())
                .imageUrl(product.getImageUrl())
                .matchScore(score)
                .matchReason(buildMatchReason(product, intent, score))
                .build();
    }

    private double scoreProduct(ProductToolResponse product, SearchIntentResult intent) {
        double score = 0;
        String text = productText(product);
        String keyword = intent.getNormalizedKeyword();
        if (StringUtils.hasText(keyword) && text.contains(keyword)) {
            score += 30;
        }
        if (StringUtils.hasText(intent.getCategoryName()) && intent.getCategoryName().equals(product.getCategoryName())) {
            score += 20;
        }
        if (tagsHit(text, intent.getSceneTags())) {
            score += 15;
        }
        if (tagsHit(text, intent.getFunctionTags())) {
            score += 15;
        }
        if (priceInRange(product.getPrice(), intent)) {
            score += 10;
        } else if (intent.getMaxPrice() != null && product.getPrice() != null
                && product.getPrice().compareTo(intent.getMaxPrice()) > 0) {
            score -= 15;
        } else if (intent.getMinPrice() != null && product.getPrice() != null
                && product.getPrice().compareTo(intent.getMinPrice()) < 0) {
            score -= 5;
        }
        if (product.getStock() != null && product.getStock() > 0) {
            score += 5;
        } else {
            score -= 20;
        }
        if (product.getSales() != null && product.getSales() > 0) {
            score += Math.min(5, product.getSales() / 100.0);
        }
        return BigDecimal.valueOf(Math.max(0, Math.min(100, score)))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String buildMatchReason(ProductToolResponse product, SearchIntentResult intent, double score) {
        List<String> reasons = new ArrayList<>();
        String text = productText(product);
        if (StringUtils.hasText(intent.getNormalizedKeyword()) && text.contains(intent.getNormalizedKeyword())) {
            reasons.add("名称或描述匹配“" + intent.getNormalizedKeyword() + "”");
        }
        if (StringUtils.hasText(intent.getCategoryName()) && intent.getCategoryName().equals(product.getCategoryName())) {
            reasons.add("分类匹配“" + intent.getCategoryName() + "”");
        }
        if (priceInRange(product.getPrice(), intent)) {
            reasons.add("价格符合预算");
        }
        addHitTagReasons(reasons, "场景", text, intent.getSceneTags());
        addHitTagReasons(reasons, "功能", text, intent.getFunctionTags());
        if (product.getStock() != null && product.getStock() > 0) {
            reasons.add("当前有库存");
        }
        if (product.getSales() != null && product.getSales() > 0) {
            reasons.add("销量 " + product.getSales());
        }
        if (reasons.isEmpty()) {
            reasons.add("来自真实商品库的相近商品");
        }
        reasons.add("匹配分 " + score);
        return String.join("；", reasons);
    }

    private void addHitTagReasons(List<String> reasons, String label, String text, List<String> tags) {
        for (String tag : safeList(tags)) {
            if (tagMatches(text, tag)) {
                reasons.add(label + "匹配“" + tag + "”");
            }
        }
    }

    private boolean tagsHit(String text, List<String> tags) {
        for (String tag : safeList(tags)) {
            if (tagMatches(text, tag)) {
                return true;
            }
        }
        return false;
    }

    private boolean tagMatches(String text, String tag) {
        if (!StringUtils.hasText(tag)) {
            return false;
        }
        if (text.contains(tag)) {
            return true;
        }
        for (String synonym : synonymWords(tag)) {
            if (text.contains(synonym)) {
                return true;
            }
        }
        return false;
    }

    private boolean priceInRange(BigDecimal price, SearchIntentResult intent) {
        if (price == null) {
            return false;
        }
        boolean minOk = intent.getMinPrice() == null || price.compareTo(intent.getMinPrice()) >= 0;
        boolean maxOk = intent.getMaxPrice() == null || price.compareTo(intent.getMaxPrice()) <= 0;
        return minOk && maxOk;
    }

    private boolean matchesExcludedTags(ProductToolResponse product, List<String> excludedTags) {
        String text = productText(product);
        for (String tag : safeList(excludedTags)) {
            if (tagMatches(text, tag)) {
                return true;
            }
        }
        return false;
    }

    private String productText(ProductToolResponse product) {
        return safe(product.getProductName()) + " " + safe(product.getCategoryName()) + " "
                + safe(product.getDescription()) + " " + safe(product.getDiscountInfo());
    }

    private String buildResponseMessage(List<SearchProductResult> products, boolean fallback) {
        if (products.isEmpty()) {
            return "没有查询到匹配商品，建议放宽预算、分类或关键词。";
        }
        if (fallback) {
            return "未找到完全匹配商品，以下为相近或热销商品。";
        }
        return "查询成功。";
    }

    private String extractKeyword(String query) {
        String[] keywords = {
                "无线耳机", "蓝牙耳机", "耳机", "机械键盘", "键盘", "显示器", "智能手表", "手机支架", "手机",
                "运动鞋", "跑鞋", "卫衣", "双肩包", "香水", "护肤套装", "咖啡", "零食礼盒", "保温杯",
                "台灯", "小风扇", "风扇", "收纳盒", "瑜伽垫", "篮球", "图书", "文具", "礼品卡", "礼盒"
        };
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                return switch (keyword) {
                    case "蓝牙耳机" -> "无线耳机";
                    case "跑鞋" -> "运动鞋";
                    case "风扇" -> "小风扇";
                    default -> keyword;
                };
            }
        }
        return query.length() > 20 ? query.substring(0, 20) : query;
    }

    private String extractCategory(String query) {
        if (containsAny(query, "无线耳机", "蓝牙耳机", "耳机", "键盘", "显示器", "手表", "数码")) {
            return "数码科技";
        }
        if (containsAny(query, "手机", "支架", "充电器", "数据线")) {
            return "手机通讯";
        }
        if (containsAny(query, "宿舍", "台灯", "收纳", "风扇", "家居", "保温杯")) {
            return "家居生活";
        }
        if (containsAny(query, "运动鞋", "跑鞋", "卫衣", "双肩包", "衣服", "鞋")) {
            return "服饰鞋包";
        }
        if (containsAny(query, "跑步", "篮球", "瑜伽", "运动")) {
            return "运动户外";
        }
        if (containsAny(query, "香水", "护肤", "美妆")) {
            return "美妆护肤";
        }
        if (containsAny(query, "咖啡", "零食", "饮料", "食品")) {
            return "食品饮料";
        }
        if (containsAny(query, "图书", "文具", "书")) {
            return "图书文具";
        }
        if (containsAny(query, "礼物", "送人", "生日", "礼品", "礼盒", "礼品卡")) {
            return "礼品专区";
        }
        return null;
    }

    private String normalizeCategory(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        return switch (categoryName.trim()) {
            case "家电", "家用", "宿舍用品" -> "家居生活";
            case "数码", "数码产品" -> "数码科技";
            case "运动", "户外" -> "运动户外";
            case "礼品", "礼物" -> "礼品专区";
            default -> categoryName.trim();
        };
    }

    private BigDecimal extractMaxPrice(String query) {
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:元|块|￥|¥)?\\s*(?:以内|以下|之内|内)").matcher(query);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        if (containsAny(query, "别太贵", "不贵", "便宜", "低价", "实惠")) {
            return BigDecimal.valueOf(100);
        }
        return null;
    }

    private BigDecimal extractMinPrice(String query) {
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:元|块|￥|¥)?\\s*(?:以上|起)").matcher(query);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private List<String> extractSceneTags(String query) {
        List<String> tags = new ArrayList<>();
        addIfContains(tags, query, "宿舍");
        addIfContains(tags, query, "跑步");
        addIfContains(tags, query, "运动");
        addIfContains(tags, query, "生日");
        addIfContains(tags, query, "送人");
        addIfContains(tags, query, "办公");
        addIfContains(tags, query, "旅行");
        addIfContains(tags, query, "学生");
        return tags;
    }

    private List<String> extractFunctionTags(String query) {
        List<String> tags = new ArrayList<>();
        if (containsAny(query, "安静", "静音", "低噪")) {
            tags.add("静音");
        }
        if (containsAny(query, "别太贵", "不贵", "便宜", "低价", "实惠")) {
            tags.add("低价");
        }
        if (containsAny(query, "轻", "轻便", "不要太重")) {
            tags.add("轻便");
        }
        if (containsAny(query, "防水", "防汗")) {
            tags.add("防水");
        }
        if (containsAny(query, "大容量", "容量大")) {
            tags.add("大容量");
        }
        if (containsAny(query, "续航", "耐用")) {
            tags.add("长续航");
        }
        return tags;
    }

    private List<String> extractExcludedTags(String query) {
        List<String> tags = new ArrayList<>();
        if (containsAny(query, "不要太重", "别太重", "不重")) {
            tags.add("厚重");
            tags.add("笨重");
        }
        if (containsAny(query, "不要贵", "别太贵")) {
            tags.add("高端");
            tags.add("奢华");
        }
        return tags;
    }

    private String inferSortBy(String query) {
        if (containsAny(query, "便宜", "低价", "不贵", "别太贵", "价格低", "从低到高")) {
            return SORT_PRICE_ASC;
        }
        if (containsAny(query, "贵一点", "高端", "从高到低")) {
            return SORT_PRICE_DESC;
        }
        if (containsAny(query, "热销", "销量", "热门")) {
            return SORT_SALES_DESC;
        }
        return SORT_RELEVANCE;
    }

    private List<String> expandKeywords(String query, SearchIntentResult intent) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String tag : mergeTags(intent)) {
            keywords.addAll(synonymWords(tag));
        }
        if (containsAny(query, "安静")) {
            keywords.add("静音");
        }
        if (containsAny(query, "不贵", "便宜", "别太贵")) {
            keywords.add("低价");
            keywords.add("实惠");
            keywords.add("特价");
        }
        if (containsAny(query, "宿舍")) {
            keywords.add("小型");
            keywords.add("便携");
            keywords.add("台灯");
            keywords.add("收纳");
        }
        if (containsAny(query, "跑步")) {
            keywords.add("运动");
            keywords.add("轻便");
            keywords.add("防水");
        }
        if (containsAny(query, "送人", "礼物", "生日")) {
            keywords.add("礼物");
            keywords.add("礼盒");
            keywords.add("礼品卡");
        }
        if (containsAny(query, "办公")) {
            keywords.add("商务");
            keywords.add("耐用");
            keywords.add("便携");
        }
        if (containsAny(query, "无线耳机", "蓝牙耳机")) {
            keywords.add("耳机");
        }
        addIfText(keywords, intent.getCategoryName());
        return new ArrayList<>(keywords);
    }

    private List<String> synonymWords(String tag) {
        if (!StringUtils.hasText(tag)) {
            return List.of();
        }
        Map<String, List<String>> map = Map.ofEntries(
                Map.entry("安静", List.of("静音", "低噪")),
                Map.entry("静音", List.of("安静", "低噪")),
                Map.entry("不贵", List.of("低价", "实惠", "特价")),
                Map.entry("便宜", List.of("低价", "实惠", "特价")),
                Map.entry("低价", List.of("便宜", "实惠", "特价")),
                Map.entry("宿舍", List.of("小型", "便携", "台灯", "收纳")),
                Map.entry("跑步", List.of("运动", "轻便", "防水")),
                Map.entry("送人", List.of("礼物", "礼盒", "礼品卡")),
                Map.entry("礼物", List.of("送人", "礼盒", "礼品卡")),
                Map.entry("办公", List.of("商务", "便携", "耐用")),
                Map.entry("防水", List.of("防汗", "防泼水", "运动")),
                Map.entry("大容量", List.of("容量大", "耐用")),
                Map.entry("长续航", List.of("续航", "电池", "耐用"))
        );
        return map.getOrDefault(tag, List.of());
    }

    private String normalizeSortBy(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return SORT_RELEVANCE;
        }
        String value = sortBy.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "PRICE_ASC", "PRICEASC", "PRICE_LOW", "价格升序", "低价优先" -> SORT_PRICE_ASC;
            case "PRICE_DESC", "PRICEDESC", "PRICE_HIGH", "价格降序", "高价优先" -> SORT_PRICE_DESC;
            case "SALES_DESC", "SALESDESC", "HOT", "销量优先", "热销优先" -> SORT_SALES_DESC;
            default -> SORT_RELEVANCE;
        };
    }

    private List<String> mergeTags(SearchIntentResult intent) {
        return mergeList(intent.getSceneTags(), intent.getFunctionTags());
    }

    private List<String> mergeList(List<String> first, List<String> second) {
        Set<String> set = new LinkedHashSet<>();
        if (first != null) {
            first.stream().filter(StringUtils::hasText).map(String::trim).forEach(set::add);
        }
        if (second != null) {
            second.stream().filter(StringUtils::hasText).map(String::trim).forEach(set::add);
        }
        return new ArrayList<>(set);
    }

    private <T> List<T> paginate(List<T> records, int page, int size) {
        int from = Math.min((page - 1) * size, records.size());
        int to = Math.min(from + size, records.size());
        return records.subList(from, to);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }

    private String extractJson(String content) {
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
        return text;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !StringUtils.hasText(value.asText())) {
            return null;
        }
        return value.isNumber() ? value.decimalValue() : new BigDecimal(value.asText());
    }

    private List<String> arrayText(JsonNode node, String field) {
        JsonNode array = node.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : array) {
            String text = item.asText();
            if (StringUtils.hasText(text)) {
                values.add(text.trim());
            }
        }
        return values;
    }

    private String firstText(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private String cleanText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String clean = text.trim();
        return "null".equalsIgnoreCase(clean) ? null : clean;
    }

    private void addIfContains(List<String> tags, String query, String tag) {
        if (query.contains(tag)) {
            tags.add(tag);
        }
    }

    private void addIfText(Set<String> values, String value) {
        if (StringUtils.hasText(value)) {
            values.add(value.trim());
        }
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record SearchResult(List<ProductToolResponse> products, boolean fallback) {
    }
}
