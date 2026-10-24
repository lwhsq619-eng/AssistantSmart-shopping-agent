package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorySaveDTO {
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private String description;
    private Integer sortNo = 0;
    private Integer status = 1;
    private String imageUrl;
}
