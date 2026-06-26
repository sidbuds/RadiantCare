package com.xixin.health.report.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 生成报告请求参数
 */
@Data
public class GenerateReportRequest {
    @NotBlank
    private String taskNo;
    private String templateCode;
}
