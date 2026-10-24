package com.example.mall.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageQueryDTO {
    private String orderNo;
    private String userName;
    private String status;
}
