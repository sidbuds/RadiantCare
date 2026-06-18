package com.xixin.health.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_result")
public class ExamResultEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String resultNo;
    private Long taskId;
    private Long taskItemId;
    private String taskItemNo;
    private Long userId;
    private Long reportId;
    private String itemCode;
    private String itemName;
    private String resultType;
    private String resultValue;
    private BigDecimal resultNumber;
    private String unit;
    private String refRange;
    private String conclusion;
    private Integer isAbnormal;
    private Integer abnormalLevel;
    private Long entryDoctorId;
    private String entryDoctorName;
    private LocalDateTime entryTime;
    private Integer auditStatus;
}
