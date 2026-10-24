package com.example.mall.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQueryDTO extends PageQueryDTO {
    private String name;
    private Long categoryId;
    private Integer status;
}
