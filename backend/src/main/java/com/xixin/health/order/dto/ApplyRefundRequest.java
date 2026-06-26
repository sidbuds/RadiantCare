package com.xixin.health.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 申请退款请求参数
 */
@Data
public class ApplyRefundRequest {
    @NotBlank
    private String reason;
}