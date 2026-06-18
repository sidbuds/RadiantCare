package com.xixin.health.consultation.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateConsultationRequest {
    @NotBlank
    private String reportNo;
    @NotBlank
    private String sourceType;
    @NotBlank
    private String consultationType;
    private String consultationTitle;
    @NotBlank
    private String consultationContent;
    private Integer priorityLevel;
}
