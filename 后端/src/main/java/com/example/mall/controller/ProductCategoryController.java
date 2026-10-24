package com.example.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mall.common.Result;
import com.example.mall.dto.CategoryQueryDTO;
import com.example.mall.dto.CategorySaveDTO;
import com.example.mall.entity.ProductCategory;
import com.example.mall.service.ProductCategoryService;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/categories")
public class ProductCategoryController {
    private final ProductCategoryService categoryService;

    public ProductCategoryController(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<ProductCategory>> categories() {
        List<ProductCategory> categories = categoryService.lambdaQuery()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortNo)
                .list();
        return Result.success("查询成功", categories);
    }

    @GetMapping("/page")
    public Result<IPage<ProductCategory>> page(@Valid CategoryQueryDTO query) {
        return Result.success("查询成功", categoryService.pageCategories(query));
    }

    @GetMapping("/list")
    public Result<List<ProductCategory>> list(@RequestParam(required = false) Integer status) {
        List<ProductCategory> categories = categoryService.lambdaQuery()
                .eq(status != null, ProductCategory::getStatus, status)
                .orderByAsc(ProductCategory::getSortNo)
                .list();
        return Result.success("查询成功", categories);
    }

    @GetMapping("/{id}")
    public Result<ProductCategory> get(@PathVariable Long id) {
        return Result.success("查询成功", categoryService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategorySaveDTO dto) {
        categoryService.createCategory(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategorySaveDTO dto) {
        categoryService.updateCategory(id, dto);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }
}
