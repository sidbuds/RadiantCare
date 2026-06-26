package com.xixin.health.operator.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 套餐检查项请求参数
 */
@Data
public class PackageItemRequest {
    @NotBlank(message = "itemCode不能为空")
    private String itemCode;
    @NotBlank(message = "itemName不能为空")
    private String itemName;
    private String unit;
    private String refRange;
    private Integer sortNo;
}
