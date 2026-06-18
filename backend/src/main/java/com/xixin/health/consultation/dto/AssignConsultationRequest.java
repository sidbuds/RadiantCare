package com.xixin.health.consultation.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AssignConsultationRequest {
    @NotNull
    private Long doctorId;
}
