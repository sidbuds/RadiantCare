package com.xixin.health.compare.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建对比任务请求参数
 */
@Data
public class CreateCompareTaskRequest {

    @NotBlank(message = "baselineReportNo不能为空")
    private String baselineReportNo;

    @NotBlank(message = "compareReportNo不能为空")
    private String compareReportNo;
}
