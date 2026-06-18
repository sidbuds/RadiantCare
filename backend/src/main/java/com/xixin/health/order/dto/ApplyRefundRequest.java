package com.xixin.health.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ApplyRefundRequest {
    @NotBlank
    private String reason;
}