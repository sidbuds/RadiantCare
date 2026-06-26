package com.xixin.health.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建订单请求参数
 */
@Data
public class CreateOrderRequest {
    @NotBlank
    private String appointmentNo;
    private String couponCode;
    private List<String> extraItemCodes;
}
