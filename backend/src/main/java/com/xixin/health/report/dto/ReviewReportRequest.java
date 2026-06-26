package com.xixin.health.report.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 审核报告请求参数
 */
@Data
public class ReviewReportRequest {
    @NotBlank
    private String reviewStage;
    @NotBlank
    private String reviewStatus;
    private String reviewComment;
}
