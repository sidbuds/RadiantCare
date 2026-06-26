package com.xixin.health.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 体检任务检查项实体
 */
@Data
@TableName("exam_task_item")
public class ExamTaskItemEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskItemNo;
    private Long taskId;
    private String taskNo;
    private String itemCode;
    private String itemName;
    private String departmentCode;
    private String departmentName;
    private Long doctorId;
    private String doctorName;
    private String roomNo;
    private Integer routeSort;
    private Integer itemStatus;
    private Integer entryStatus;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String skipReason;
    private String remark;
}
