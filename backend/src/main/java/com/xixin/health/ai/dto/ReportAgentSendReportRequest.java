package com.xixin.health.ai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReportAgentSendReportRequest {
    @NotBlank(message = "报告编号不能为空")
    private String reportNo;
}
