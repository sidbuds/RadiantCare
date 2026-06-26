package com.xixin.health.operator.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 保存套餐请求参数
 */
@Data
public class SavePackageRequest {
    @NotBlank(message = "packageCode不能为空")
    private String packageCode;
    @NotBlank(message = "packageName不能为空")
    private String packageName;
    private String category;
    @NotNull(message = "price不能为空")
    @DecimalMin(value = "0.00", message = "price不能小于0")
    private BigDecimal price;
    @NotNull(message = "status不能为空")
    private Integer status;
    private String remark;
    private String templateCode;
    @Valid
    private List<PackageItemRequest> items;
}
