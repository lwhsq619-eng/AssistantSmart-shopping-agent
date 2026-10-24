package com.example.mall.agent.tool;

import com.example.mall.agent.dto.tool.ProductCompareResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.entity.Product;
import com.example.mall.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 商品业务 Tool。
 * 只调用已有 ProductService，不直接访问 Mapper，不拼接 SQL。
 */
@Component
public class ProductTool {
    private static final Logger log = LoggerFactory.getLogger(ProductTool.class);

    private final ProductService productService;

    public ProductTool(ProductService productService) {
        this.productService = productService;
    }

    public List<ProductToolResponse> searchProducts(String keyword, String categoryName, BigDecimal minPrice, BigDecimal maxPrice) {
        long start = System.currentTimeMillis();
        try {
            List<ProductToolResponse> result = productService.lambdaQuery()
                    .eq(Product::getStatus, 1)
                    .list()
                    .stream()
                    .filter(product -> matchesKeyword(product, keyword))
                    .filter(product -> !StringUtils.hasText(categoryName)
                            || (product.getCategoryName() != null && product.getCategoryName().contains(categoryName)))
                    .filter(product -> minPrice == null || comparePrice(product).compareTo(minPrice) >= 0)
                    .filter(product -> maxPrice == null || comparePrice(product).compareTo(maxPrice) <= 0)
                    .sorted(Comparator.comparing(this::hasRealImage).reversed()
                            .thenComparing(Product::getSales, Comparator.nullsLast(Integer::compareTo).reversed()))
                    .limit(20)
                    .map(this::toProductResponse)
                    .toList();
            log.info("Tool call success, tool=ProductTool.searchProducts, keyword={}, categoryName={}, elapsedMs={}, count={}",
                    keyword, categoryName, System.currentTimeMillis() - start, result.size());
            return result;
        } catch (Exception e) {
            log.error("Tool call failed, tool=ProductTool.searchProducts, keyword={}, categoryName={}", keyword, categoryName, e);
            throw e;
        }
    }

    public ProductToolResponse getProductDetail(Long productId) {
        long start = System.currentTimeMillis();
        Product product = requireProduct(productId);
        ProductToolResponse result = toProductResponse(product);
        log.info("Tool call success, tool=ProductTool.getProductDetail, productId={}, elapsedMs={}, count=1",
                productId, System.currentTimeMillis() - start);
        return result;
    }

    public ProductCompareResponse compareProducts(List<Long> productIds) {
        long start = System.currentTimeMillis();
        if (productIds == null || productIds.isEmpty()) {
            throw new AiException(400, "商品ID列表不能为空");
        }
        List<ProductToolResponse> products = productService.listByIds(productIds)
                .stream()
                .map(this::toProductResponse)
                .toList();
        String summary = products.isEmpty()
                ? "未找到可对比商品"
                : "已找到 " + products.size() + " 个商品，可从价格、库存、销量进行对比。";
        log.info("Tool call success, tool=ProductTool.compareProducts, productIds={}, elapsedMs={}, count={}",
                productIds, System.currentTimeMillis() - start, products.size());
        return ProductCompareResponse.builder().products(products).summary(summary).build();
    }

    public List<ProductToolResponse> getHotProducts(Integer limit) {
        long start = System.currentTimeMillis();
        int safeLimit = normalizeLimit(limit);
        List<ProductToolResponse> result = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .list()
                .stream()
                .sorted(Comparator.comparing(this::hasRealImage).reversed()
                        .thenComparing(Product::getSales, Comparator.nullsLast(Integer::compareTo).reversed()))
                .limit(safeLimit)
                .map(this::toProductResponse)
                .toList();
        log.info("Tool call success, tool=ProductTool.getHotProducts, limit={}, elapsedMs={}, count={}",
                safeLimit, System.currentTimeMillis() - start, result.size());
        return result;
    }

    public List<ProductToolResponse> getDiscountProducts(Integer limit) {
        long start = System.currentTimeMillis();
        int safeLimit = normalizeLimit(limit);
        List<ProductToolResponse> result = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .eq(Product::getIsSpecial, 1)
                .list()
                .stream()
                .sorted(Comparator.comparing(this::hasRealImage).reversed()
                        .thenComparing(this::comparePrice))
                .limit(safeLimit)
                .map(this::toProductResponse)
                .toList();
        log.info("Tool call success, tool=ProductTool.getDiscountProducts, limit={}, elapsedMs={}, count={}",
                safeLimit, System.currentTimeMillis() - start, result.size());
        return result;
    }

    private Product requireProduct(Long productId) {
        if (productId == null) {
            throw new AiException(400, "商品ID不能为空");
        }
        Product product = productService.getById(productId);
        if (product == null) {
            throw new AiException(404, "商品不存在");
        }
        return product;
    }

    private boolean matchesKeyword(Product product, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return (product.getName() != null && product.getName().contains(keyword))
                || (product.getDescription() != null && product.getDescription().contains(keyword))
                || (product.getBrandName() != null && product.getBrandName().contains(keyword));
    }

    private ProductToolResponse toProductResponse(Product product) {
        BigDecimal displayPrice = comparePrice(product);
        String discountInfo = product.getDiscountPrice() != null && product.getMarketPrice() != null
                ? "原价" + product.getMarketPrice() + "，现价" + product.getDiscountPrice()
                : "暂无折扣";
        return ProductToolResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .categoryName(product.getCategoryName())
                .price(displayPrice)
                .stock(product.getStock())
                .sales(product.getSales())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .discountInfo(discountInfo)
                .build();
    }

    private BigDecimal comparePrice(Product product) {
        if (product.getDiscountPrice() != null) {
            return product.getDiscountPrice();
        }
        if (product.getPrice() != null) {
            return product.getPrice();
        }
        return BigDecimal.ZERO;
    }

    private boolean hasRealImage(Product product) {
        return product != null
                && product.getImageUrl() != null
                && product.getImageUrl().startsWith("https://images.unsplash.com/");
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }
}
