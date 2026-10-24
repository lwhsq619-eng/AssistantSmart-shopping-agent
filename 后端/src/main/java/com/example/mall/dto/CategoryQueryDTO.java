package com.example.mall.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryQueryDTO extends PageQueryDTO {
    private String name;
    private Integer status;
}
