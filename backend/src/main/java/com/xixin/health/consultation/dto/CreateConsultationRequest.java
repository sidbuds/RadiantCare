package com.xixin.health.consultation.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CreateConsultationRequest {
    @NotBlank(message = "报告编号不能为空")
    @Size(max = 64, message = "报告编号最长64个字符")
    private String reportNo;

    @NotBlank(message = "来源类型不能为空")
    @Size(max = 32, message = "来源类型最长32个字符")
    private String sourceType;

    @NotBlank(message = "咨询类型不能为空")
    @Size(max = 32, message = "咨询类型最长32个字符")
    private String consultationType;

    @Size(max = 128, message = "咨询标题最长128个字符")
    private String consultationTitle;

    @NotBlank(message = "咨询内容不能为空")
    @Size(max = 2000, message = "咨询内容最长2000个字符")
    private String consultationContent;

    private Integer priorityLevel;
}
