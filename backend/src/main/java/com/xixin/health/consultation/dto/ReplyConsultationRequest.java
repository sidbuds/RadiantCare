package com.xixin.health.consultation.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 回复咨询请求参数
 */
@Data
public class ReplyConsultationRequest {
    @NotBlank
    private String replyContent;
    private String attachmentUrl;
}
