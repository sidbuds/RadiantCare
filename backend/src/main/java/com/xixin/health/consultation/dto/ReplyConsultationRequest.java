package com.xixin.health.consultation.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReplyConsultationRequest {
    @NotBlank
    private String replyContent;
    private String attachmentUrl;
}
