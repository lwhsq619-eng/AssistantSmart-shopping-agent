package com.example.mall.agent.tool;

import com.example.mall.agent.dto.tool.CommentSummaryResponse;
import com.example.mall.agent.dto.tool.CommentToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.entity.Product;
import com.example.mall.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评论业务 Tool。
 * 当前项目存在评论数据表扩展痕迹，但没有 CommentService / ReviewService。
 * 按“Tool 不直接调用 Mapper、不直接写 SQL”的约束，本 Tool 暂时返回空集合。
 */
@Component
public class CommentTool {
    private static final Logger log = LoggerFactory.getLogger(CommentTool.class);

    private final ProductService productService;

    public CommentTool(ProductService productService) {
        this.productService = productService;
    }

    public List<CommentToolResponse> getProductComments(Long productId, Integer limit) {
        long start = System.currentTimeMillis();
        requireProduct(productId);
        log.info("Tool call success, tool=CommentTool.getProductComments, productId={}, limit={}, elapsedMs={}, count=0",
                productId, normalizeLimit(limit), System.currentTimeMillis() - start);
        return List.of();
    }

    public List<CommentToolResponse> getNegativeComments(Long productId, Integer limit) {
        long start = System.currentTimeMillis();
        requireProduct(productId);
        log.info("Tool call success, tool=CommentTool.getNegativeComments, productId={}, limit={}, elapsedMs={}, count=0",
                productId, normalizeLimit(limit), System.currentTimeMillis() - start);
        return List.of();
    }

    public CommentSummaryResponse getCommentSummary(Long productId) {
        long start = System.currentTimeMillis();
        requireProduct(productId);
        CommentSummaryResponse result = CommentSummaryResponse.builder()
                .productId(productId)
                .totalCount(0)
                .positiveCount(0)
                .negativeCount(0)
                .averageRating(BigDecimal.ZERO)
                .summary("当前项目未提供评论 Service，AI Tool 按约束返回空评论摘要。")
                .build();
        log.info("Tool call success, tool=CommentTool.getCommentSummary, productId={}, elapsedMs={}, count=1",
                productId, System.currentTimeMillis() - start);
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

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }
}
