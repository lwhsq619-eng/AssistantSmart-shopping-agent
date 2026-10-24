package com.example.mall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSaveDTO {
    private String productNo;
    @NotBlank(message = "商品名称不能为空")
    private String name;
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;
    private String imageUrl;
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
    private String description;
    private Integer status = 1;
}
