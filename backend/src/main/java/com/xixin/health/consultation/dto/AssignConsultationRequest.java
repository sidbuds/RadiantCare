package com.xixin.health.consultation.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 分配咨询请求参数
 */
@Data
public class AssignConsultationRequest {
    @NotNull
    private Long doctorId;
}
