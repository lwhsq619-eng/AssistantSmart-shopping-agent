package com.example.mall.agent.tool;

import com.example.mall.agent.dto.tool.InventoryToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.service.OrderItemService;
import com.example.mall.service.OrdersService;
import com.example.mall.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 库存业务 Tool。
 * 当前项目没有独立库存 Service，因此使用已有 ProductService 的 stock 字段和订单 Service 计算销量。
 */
@Component
public class InventoryTool {
    private static final Logger log = LoggerFactory.getLogger(InventoryTool.class);

    private final ProductService productService;
    private final OrdersService ordersService;
    private final OrderItemService orderItemService;

    public InventoryTool(ProductService productService,
                         OrdersService ordersService,
                         OrderItemService orderItemService) {
        this.productService = productService;
        this.ordersService = ordersService;
        this.orderItemService = orderItemService;
    }

    public InventoryToolResponse getInventory(Long productId) {
        long start = System.currentTimeMillis();
        Product product = requireProduct(productId);
        InventoryToolResponse result = toInventoryResponse(product, 7);
        log.info("Tool call success, tool=InventoryTool.getInventory, productId={}, elapsedMs={}, count=1",
                productId, System.currentTimeMillis() - start);
        return result;
    }

    public List<InventoryToolResponse> getLowStockProducts(Integer threshold) {
        long start = System.currentTimeMillis();
        int safeThreshold = threshold == null || threshold < 0 ? 10 : threshold;
        List<InventoryToolResponse> result = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .list()
                .stream()
                .filter(product -> product.getStock() != null && product.getStock() <= safeThreshold)
                .sorted(Comparator.comparing(Product::getStock, Comparator.nullsLast(Integer::compareTo)))
                .limit(50)
                .map(product -> toInventoryResponse(product, 7))
                .toList();
        log.info("Tool call success, tool=InventoryTool.getLowStockProducts, threshold={}, elapsedMs={}, count={}",
                safeThreshold, System.currentTimeMillis() - start, result.size());
        return result;
    }

    public InventoryToolResponse getSalesAndStock(Long productId) {
        long start = System.currentTimeMillis();
        Product product = requireProduct(productId);
        InventoryToolResponse result = toInventoryResponse(product, 30);
        log.info("Tool call success, tool=InventoryTool.getSalesAndStock, productId={}, elapsedMs={}, count=1",
                productId, System.currentTimeMillis() - start);
        return result;
    }

    public List<InventoryToolResponse> getSlowMovingProducts(Integer days, Integer limit) {
        long start = System.currentTimeMillis();
        int safeDays = days == null || days <= 0 ? 30 : Math.min(days, 365);
        int safeLimit = normalizeLimit(limit);
        List<InventoryToolResponse> result = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .list()
                .stream()
                .map(product -> toInventoryResponse(product, safeDays))
                .sorted(Comparator.comparing(InventoryToolResponse::getAverageDailySales)
                        .thenComparing(InventoryToolResponse::getStock, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
        log.info("Tool call success, tool=InventoryTool.getSlowMovingProducts, days={}, limit={}, elapsedMs={}, count={}",
                safeDays, safeLimit, System.currentTimeMillis() - start, result.size());
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

    private InventoryToolResponse toInventoryResponse(Product product, int averageDays) {
        int sales7d = calculateSales(product.getId(), 7);
        int sales30d = calculateSales(product.getId(), 30);
        BigDecimal averageDailySales = BigDecimal.valueOf(calculateSales(product.getId(), averageDays))
                .divide(BigDecimal.valueOf(averageDays), 2, RoundingMode.HALF_UP);
        BigDecimal estimatedDaysToSellOut = calculateSellOutDays(product.getStock(), averageDailySales);
        return InventoryToolResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .stock(product.getStock())
                .sales7d(sales7d)
                .sales30d(sales30d)
                .averageDailySales(averageDailySales)
                .estimatedDaysToSellOut(estimatedDaysToSellOut)
                .suggestion(buildSuggestion(product.getStock(), averageDailySales, estimatedDaysToSellOut))
                .build();
    }

    private int calculateSales(Long productId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Long> orderIds = ordersService.lambdaQuery()
                .ge(Orders::getOrderTime, startTime)
                .ne(Orders::getStatus, "已取消")
                .list()
                .stream()
                .map(Orders::getId)
                .toList();
        if (orderIds.isEmpty()) {
            return 0;
        }
        return orderItemService.lambdaQuery()
                .in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getProductId, productId)
                .list()
                .stream()
                .map(OrderItem::getQuantity)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private BigDecimal calculateSellOutDays(Integer stock, BigDecimal averageDailySales) {
        if (stock == null || stock <= 0) {
            return BigDecimal.ZERO;
        }
        if (averageDailySales == null || averageDailySales.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.valueOf(9999);
        }
        return BigDecimal.valueOf(stock).divide(averageDailySales, 1, RoundingMode.HALF_UP);
    }

    private String buildSuggestion(Integer stock, BigDecimal averageDailySales, BigDecimal estimatedDaysToSellOut) {
        int safeStock = stock == null ? 0 : stock;
        if (safeStock <= 0) {
            return "库存已售罄，建议立即补货或下架处理。";
        }
        if (safeStock < 10) {
            return "库存偏低，建议尽快补货。";
        }
        if (averageDailySales.compareTo(BigDecimal.ZERO) == 0) {
            return "近期无销量，建议优化标题、图片或活动曝光。";
        }
        if (estimatedDaysToSellOut.compareTo(BigDecimal.valueOf(7)) <= 0) {
            return "预计一周内售罄，建议提前备货。";
        }
        return "库存状态正常，持续观察销量变化。";
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }
}
