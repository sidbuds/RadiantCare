package com.xixin.health.exam.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamResultEntryDto {
    private String metricCode;
    private String metricName;
    private String resultValue;
    private BigDecimal resultNumber;
    private String unit;
    private String refRange;
    private Boolean abnormal;
    private Integer abnormalLevel;
}
