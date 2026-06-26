package com.xixin.health.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体检任务实体
 */
@Data
@TableName("exam_task")
public class ExamTaskEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private Long appointmentId;
    private String appointmentNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long packageId;
    private String centerCode;
    private LocalDate taskDate;
    private Integer taskStatus;
    private Integer reportStatus;
    private Integer guideStatus;
    private LocalDateTime arriveTime;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String remark;
}
