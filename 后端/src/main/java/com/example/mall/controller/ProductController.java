package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.Result;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.entity.Brand;
import com.example.mall.entity.Product;
import com.example.mall.entity.ProductImage;
import com.example.mall.entity.ProductReview;
import com.example.mall.entity.ProductSku;
import com.example.mall.mapper.BrandMapper;
import com.example.mall.mapper.ProductImageMapper;
import com.example.mall.mapper.ProductReviewMapper;
import com.example.mall.mapper.ProductSkuMapper;
import com.example.mall.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final String REAL_IMAGE_ORDER_SQL = "CASE WHEN image_url LIKE 'https://images.unsplash.com/%' THEN 1 ELSE 0 END";

    private final ProductService productService;
    private final BrandMapper brandMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductReviewMapper productReviewMapper;

    public ProductController(ProductService productService,
                             BrandMapper brandMapper,
                             ProductImageMapper productImageMapper,
                             ProductSkuMapper productSkuMapper,
                             ProductReviewMapper productReviewMapper) {
        this.productService = productService;
        this.brandMapper = brandMapper;
        this.productImageMapper = productImageMapper;
        this.productSkuMapper = productSkuMapper;
        this.productReviewMapper = productReviewMapper;
    }

    @GetMapping
    public Result<IPage<Product>> products(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(required = false) Long page,
                                           @RequestParam(defaultValue = "20") Long size,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Long brandId,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Integer isSpecial,
                                           @RequestParam(required = false) Integer isRecommended,
                                           @RequestParam(required = false) Integer isNew) {
        long safeCurrent = Math.max(page != null ? page : (current == null ? 1L : current), 1L);
        long safeSize = Math.min(Math.max(size == null ? 20L : size, 1L), 100L);
        Integer safeStatus = status == null ? 1 : status;
        QueryWrapper<Product> wrapper = new QueryWrapper<Product>()
                .eq("status", safeStatus)
                .eq(categoryId != null, "category_id", categoryId)
                .eq(brandId != null, "brand_id", brandId)
                .eq(isSpecial != null, "is_special", isSpecial)
                .eq(isRecommended != null, "is_recommended", isRecommended)
                .eq(isNew != null, "is_new", isNew);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        wrapper.orderByDesc(REAL_IMAGE_ORDER_SQL)
                .orderByDesc("is_recommended")
                .orderByDesc("is_new")
                .orderByDesc("sales");
        IPage<Product> productPage = productService.page(new Page<>(safeCurrent, safeSize), wrapper);
        return Result.success("查询成功", productPage);
    }

    @GetMapping("/search")
    public Result<IPage<Product>> search(@RequestParam(defaultValue = "1") Long current,
                                         @RequestParam(required = false) Long page,
                                         @RequestParam(defaultValue = "20") Long size,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        return products(current, page, size, categoryId, null, keyword, status, null, null, null);
    }

    @GetMapping("/special")
    public Result<List<Product>> special() {
        QueryWrapper<Product> query = new QueryWrapper<Product>()
                .eq("status", 1)
                .eq("is_special", 1)
                .orderByDesc(REAL_IMAGE_ORDER_SQL)
                .orderByAsc("discount_price")
                .last("limit 12");
        return Result.success("查询成功", productService.list(query));
    }

    @GetMapping("/recommend")
    public Result<List<Product>> recommend(@RequestParam(defaultValue = "hot") String type) {
        QueryWrapper<Product> query = new QueryWrapper<Product>()
                .eq("status", 1)
                .orderByDesc(REAL_IMAGE_ORDER_SQL);
        if ("new".equals(type)) {
            query.orderByDesc("create_time");
        } else if ("guess".equals(type)) {
            query.orderByDesc("rating").orderByDesc("review_count");
        } else {
            query.orderByDesc("sales");
        }
        query.last("limit 12");
        return Result.success("查询成功", productService.list(query));
    }

    @GetMapping("/similar/{id}")
    public Result<List<Product>> similar(@PathVariable Long id) {
        Product current = productService.getById(id);
        if (current == null) {
            return Result.success("查询成功", List.of());
        }
        QueryWrapper<Product> query = new QueryWrapper<Product>()
                .eq("status", 1)
                .ne("id", id)
                .and(w -> {
                    w.eq("category_id", current.getCategoryId());
                    if (current.getBrandId() != null) {
                        w.or().eq("brand_id", current.getBrandId());
                    }
                })
                .orderByDesc(REAL_IMAGE_ORDER_SQL)
                .orderByDesc("sales")
                .last("limit 8");
        List<Product> products = productService.list(query);
        return Result.success("查询成功", products);
    }

    @GetMapping("/page")
    public Result<IPage<Product>> page(@Valid ProductQueryDTO query) {
        return Result.success("查询成功", productService.pageProducts(query));
    }

    @GetMapping("/list")
    public Result<List<Product>> list(@RequestParam(required = false) Integer status) {
        QueryWrapper<Product> query = new QueryWrapper<Product>()
                .eq(status != null, "status", status)
                .orderByDesc(REAL_IMAGE_ORDER_SQL)
                .orderByDesc("create_time");
        List<Product> products = productService.list(query);
        return Result.success("查询成功", products);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        Product product = productService.getById(id);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("product", product);
        if (product != null) {
            Brand brand = product.getBrandId() == null ? null : brandMapper.selectById(product.getBrandId());
            List<ProductImage> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                    .eq(ProductImage::getProductId, id)
                    .orderByAsc(ProductImage::getSortNo));
            List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getProductId, id));
            List<ProductReview> reviews = productReviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                    .eq(ProductReview::getProductId, id)
                    .orderByDesc(ProductReview::getCreateTime)
                    .last("limit 10"));
            detail.put("brand", brand);
            detail.put("images", images);
            detail.put("skus", skus);
            detail.put("reviews", reviews);
        }
        return Result.success("查询成功", detail);
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductSaveDTO dto) {
        productService.createProduct(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        productService.updateProduct(id, dto);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
