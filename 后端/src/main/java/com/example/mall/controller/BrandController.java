package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.entity.Brand;
import com.example.mall.entity.Product;
import com.example.mall.mapper.BrandMapper;
import com.example.mall.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brands")
public class BrandController {
    private final BrandMapper brandMapper;
    private final ProductService productService;

    public BrandController(BrandMapper brandMapper, ProductService productService) {
        this.brandMapper = brandMapper;
        this.productService = productService;
    }

    @GetMapping
    public Result<List<Brand>> list() {
        return Result.success("查询成功", brandMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Brand>()
                .eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getId)));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Brand brand = brandMapper.selectById(id);
        List<Product> products = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .eq(Product::getBrandId, id)
                .orderByDesc(Product::getSales)
                .list();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("brand", brand);
        data.put("products", products);
        return Result.success("查询成功", data);
    }
}
