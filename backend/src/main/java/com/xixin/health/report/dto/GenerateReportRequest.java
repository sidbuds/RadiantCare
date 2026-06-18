package com.xixin.health.report.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GenerateReportRequest {
    @NotBlank
    private String taskNo;
    private String templateCode;
}
