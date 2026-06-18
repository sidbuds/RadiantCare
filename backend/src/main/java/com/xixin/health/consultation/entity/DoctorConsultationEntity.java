package com.xixin.health.consultation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_consultation")
public class DoctorConsultationEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String consultationNo;
    private Long userId;
    private Long doctorId;
    private String doctorName;
    private Long reportId;
    private String reportNo;
    private String sourceType;
    private String consultationType;
    private String consultationTitle;
    private String consultationContent;
    private Integer consultationStatus;
    private Integer priorityLevel;
    private LocalDateTime latestReplyTime;
    private LocalDateTime closedTime;
}
