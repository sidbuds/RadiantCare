package com.xixin.health.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 生成体检任务请求参数
 */
@Data
public class GenerateExamTaskRequest {
    @NotBlank
    private String orderNo;
}
