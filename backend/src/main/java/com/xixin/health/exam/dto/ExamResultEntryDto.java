package com.xixin.health.exam.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 检查结果条目DTO
 */
@Data
public class ExamResultEntryDto {
    @NotBlank(message = "指标编码不能为空")
    @Size(max = 64, message = "指标编码最长64个字符")
    private String metricCode;

    @NotBlank(message = "指标名称不能为空")
    @Size(max = 128, message = "指标名称最长128个字符")
    private String metricName;

    @NotBlank(message = "检查结果值不能为空")
    @Size(max = 512, message = "检查结果值最长512个字符")
    private String resultValue;

    private BigDecimal resultNumber;

    @Size(max = 32, message = "单位最长32个字符")
    private String unit;

    @Size(max = 64, message = "参考范围最长64个字符")
    private String refRange;

    private Boolean abnormal;

    @Min(value = 0, message = "异常等级最小为0")
    @Max(value = 3, message = "异常等级最大为3")
    private Integer abnormalLevel;
}
