package com.xixin.health.consultation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xixin.health.common.model.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询回复实体
 */
@Data
@TableName("doctor_consultation_reply")
public class DoctorConsultationReplyEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long consultationId;
    private String consultationNo;
    private String replyRole;
    private Long replyUserId;
    private String replyUserName;
    private String replyContent;
    private String attachmentUrl;
    private String messageType;
    private String refReportNo;
    private LocalDateTime replyTime;
}
