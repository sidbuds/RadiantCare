package com.xixin.health.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体检报告实体
 */
@Data
@TableName("exam_report")
public class ExamReportEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportNo;
    private Long appointmentId;
    private Long taskId;
    private Long userId;
    private Long packageId;
    private Long templateId;
    private LocalDate reportDate;
    private String overallConclusion;
    private Integer status;
    private String pdfUrl;
    private Integer versionNo;
    private LocalDateTime publishedAt;
}
