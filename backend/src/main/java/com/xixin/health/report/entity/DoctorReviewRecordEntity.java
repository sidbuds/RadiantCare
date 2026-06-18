package com.xixin.health.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_review_record")
public class DoctorReviewRecordEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reviewNo;
    private Long reportId;
    private Long taskId;
    private String reviewStage;
    private String reviewStatus;
    private String reviewComment;
    private Long reviewerId;
    private String reviewerName;
    private LocalDateTime reviewedAt;
}
