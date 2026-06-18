package com.xixin.health.report.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReviewReportRequest {
    @NotBlank
    private String reviewStage;
    @NotBlank
    private String reviewStatus;
    private String reviewComment;
}
