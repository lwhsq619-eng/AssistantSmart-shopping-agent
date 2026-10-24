package com.example.mall.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQueryDTO {
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;
    @Min(value = 1, message = "每页条数必须大于0")
    private Long size = 10L;
}
