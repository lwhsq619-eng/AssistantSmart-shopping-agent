package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.entity.Product;
import com.example.mall.entity.ProductCategory;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.service.ProductCategoryService;
import com.example.mall.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    private final ProductCategoryService categoryService;

    public ProductServiceImpl(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public IPage<Product> pageProducts(ProductQueryDTO query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(query.getName()), Product::getName, query.getName())
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                .orderByDesc(Product::getCreateTime);
        return page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
    }

    @Override
    public void createProduct(ProductSaveDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        fillCategoryName(product, dto.getCategoryId());
        if (!StringUtils.hasText(product.getProductNo())) {
            product.setProductNo("SP" + System.currentTimeMillis());
        }
        LocalDateTime now = LocalDateTime.now();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        save(product);
    }

    @Override
    public void updateProduct(Long id, ProductSaveDTO dto) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        BeanUtils.copyProperties(dto, product);
        product.setId(id);
        fillCategoryName(product, dto.getCategoryId());
        product.setUpdateTime(LocalDateTime.now());
        updateById(product);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        updateById(product);
    }

    private void fillCategoryName(Product product, Long categoryId) {
        ProductCategory category = categoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException("商品分类不存在");
        }
        product.setCategoryId(categoryId);
        product.setCategoryName(category.getName());
    }
}
