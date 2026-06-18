package com.xixin.health.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RefundAuditRequest {
    @NotBlank
    private String auditRemark;
}