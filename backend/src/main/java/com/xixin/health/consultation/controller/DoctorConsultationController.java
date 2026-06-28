package com.xixin.health.consultation.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.consultation.dto.ReplyConsultationRequest;
import com.xixin.health.consultation.service.ConsultationService;
import com.xixin.health.report.service.ReportPdfStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 医生端咨询控制器
 */
@RestController
@RequestMapping("/api/doctor/consultations")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorConsultationController {

    private final ConsultationService consultationService;

    public DoctorConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /** 查询待处理咨询 */
    @GetMapping("/todo")
    public ApiResult<?> todo() {
        return ApiResult.success(consultationService.doctorTodo());
    }

    /** 查询咨询详情 */
    @GetMapping("/{consultationNo}")
    public ApiResult<?> detail(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.doctorDetail(consultationNo));
    }

    /** 回复咨询 */
    @PostMapping("/{consultationNo}/reply")
    public ApiResult<?> reply(@PathVariable String consultationNo, @Validated @RequestBody ReplyConsultationRequest request) {
        return ApiResult.success(consultationService.reply(consultationNo, request));
    }

    @GetMapping("/{consultationNo}/reports/{reportNo}/pdf/download")
    public ResponseEntity<InputStreamResource> downloadReportPdf(@PathVariable String consultationNo,
                                                                 @PathVariable String reportNo) {
        ReportPdfStorageService.StoredObject stored = consultationService.downloadSharedReportPdf(consultationNo, reportNo);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encode(reportNo + ".pdf"));
        if (stored.getContentLength() >= 0) {
            builder.contentLength(stored.getContentLength());
        }
        return builder.body(new InputStreamResource(stored.getInputStream()));
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
