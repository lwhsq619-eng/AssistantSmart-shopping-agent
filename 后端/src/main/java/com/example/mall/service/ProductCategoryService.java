package com.example.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.CategoryQueryDTO;
import com.example.mall.dto.CategorySaveDTO;
import com.example.mall.entity.ProductCategory;

public interface ProductCategoryService extends IService<ProductCategory> {
    IPage<ProductCategory> pageCategories(CategoryQueryDTO query);
    void createCategory(CategorySaveDTO dto);
    void updateCategory(Long id, CategorySaveDTO dto);
    void updateStatus(Long id, Integer status);
}
