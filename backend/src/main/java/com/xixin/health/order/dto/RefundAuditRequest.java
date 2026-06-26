package com.xixin.health.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 退款审核请求参数
 */
@Data
public class RefundAuditRequest {
    @NotBlank
    private String auditRemark;
}