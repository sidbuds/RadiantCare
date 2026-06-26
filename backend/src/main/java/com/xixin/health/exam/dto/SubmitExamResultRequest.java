package com.xixin.health.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 提交检查结果请求参数
 */
@Data
public class SubmitExamResultRequest {
    @NotBlank
    private String itemCode;
    @NotEmpty
    private List<ExamResultEntryDto> resultEntries;
    private String conclusion;
}
