package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {
    @NotBlank(message = "收货人不能为空")
    private String receiverName;
    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;
    private String province;
    private String city;
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
    private Integer isDefault = 0;
}
