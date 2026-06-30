package com.xixin.health.ai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReportAgentChatRequest {
    @NotBlank(message = "问题不能为空")
    private String question;
}
