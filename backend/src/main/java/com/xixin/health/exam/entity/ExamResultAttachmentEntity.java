package com.xixin.health.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检查结果附件实体
 */
@Data
@TableName("exam_result_attachment")
public class ExamResultAttachmentEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resultId;
    private Long taskItemId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadTime;
    private String remark;
}
