package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.CategoryQueryDTO;
import com.example.mall.dto.CategorySaveDTO;
import com.example.mall.entity.ProductCategory;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.ProductCategoryMapper;
import com.example.mall.service.ProductCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
    @Override
    public IPage<ProductCategory> pageCategories(CategoryQueryDTO query) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<ProductCategory>()
                .like(StringUtils.hasText(query.getName()), ProductCategory::getName, query.getName())
                .eq(query.getStatus() != null, ProductCategory::getStatus, query.getStatus())
                .orderByAsc(ProductCategory::getSortNo)
                .orderByDesc(ProductCategory::getCreateTime);
        return page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
    }

    @Override
    public void createCategory(CategorySaveDTO dto) {
        long count = lambdaQuery().eq(ProductCategory::getName, dto.getName()).count();
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(dto, category);
        LocalDateTime now = LocalDateTime.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        save(category);
    }

    @Override
    public void updateCategory(Long id, CategorySaveDTO dto) {
        ProductCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        long count = lambdaQuery().eq(ProductCategory::getName, dto.getName()).ne(ProductCategory::getId, id).count();
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ProductCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }
}
