package com.xixin.health.consultation.dto;

import lombok.Data;

/**
 * 咨询回复请求参数
 */
@Data
public class ReplyConsultationRequest {
    private String replyContent;
    private String attachmentUrl;
    private String messageType;
    private String refReportNo;
}
