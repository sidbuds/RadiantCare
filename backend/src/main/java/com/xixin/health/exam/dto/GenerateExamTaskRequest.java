package com.xixin.health.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GenerateExamTaskRequest {
    @NotBlank
    private String orderNo;
}
