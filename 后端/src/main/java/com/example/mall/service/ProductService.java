package com.example.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.entity.Product;

public interface ProductService extends IService<Product> {
    IPage<Product> pageProducts(ProductQueryDTO query);
    void createProduct(ProductSaveDTO dto);
    void updateProduct(Long id, ProductSaveDTO dto);
    void updateStatus(Long id, Integer status);
}
