package com.example.mall.agent.controller;

import com.example.mall.agent.dto.tool.CommentSummaryResponse;
import com.example.mall.agent.dto.tool.InventoryToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.tool.CommentTool;
import com.example.mall.agent.tool.InventoryTool;
import com.example.mall.agent.tool.OrderTool;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.tool.UserTool;
import com.example.mall.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 第二阶段 Tool 临时测试接口。
 * 仅用于验证 Agent Tool 封装，后续 Agent 内部调用 Tool 时可不经过本 Controller。
 */
@RestController
@RequestMapping("/api/ai/tool")
public class ToolTestController {
    private final ProductTool productTool;
    private final OrderTool orderTool;
    private final UserTool userTool;
    private final CommentTool commentTool;
    private final InventoryTool inventoryTool;

    public ToolTestController(ProductTool productTool,
                              OrderTool orderTool,
                              UserTool userTool,
                              CommentTool commentTool,
                              InventoryTool inventoryTool) {
        this.productTool = productTool;
        this.orderTool = orderTool;
        this.userTool = userTool;
        this.commentTool = commentTool;
        this.inventoryTool = inventoryTool;
    }

    @GetMapping("/product/search")
    public Result<List<ProductToolResponse>> searchProducts(@RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) String categoryName,
                                                            @RequestParam(required = false) BigDecimal minPrice,
                                                            @RequestParam(required = false) BigDecimal maxPrice) {
        return Result.success(productTool.searchProducts(keyword, categoryName, minPrice, maxPrice));
    }

    @GetMapping("/product/detail")
    public Result<ProductToolResponse> getProductDetail(@RequestParam Long productId) {
        return Result.success(productTool.getProductDetail(productId));
    }

    @GetMapping("/order/status")
    public Result<OrderToolResponse> getOrderStatus(@RequestParam Long orderId) {
        return Result.success(orderTool.getOrderStatus(orderId));
    }

    @GetMapping("/user/profile")
    public Result<UserToolResponse> getUserProfile(@RequestParam Long userId) {
        return Result.success(userTool.getUserProfile(userId));
    }

    @GetMapping("/comment/summary")
    public Result<CommentSummaryResponse> getCommentSummary(@RequestParam Long productId) {
        return Result.success(commentTool.getCommentSummary(productId));
    }

    @GetMapping("/inventory/low-stock")
    public Result<List<InventoryToolResponse>> getLowStockProducts(@RequestParam(required = false) Integer threshold) {
        return Result.success(inventoryTool.getLowStockProducts(threshold));
    }
}
