package com.xixin.health.report.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.report.service.ReportPdfService;
import com.xixin.health.report.service.ReportPdfStorageService;
import com.xixin.health.report.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 报告控制器 - 用户查看体检报告
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('USER')")
public class ReportController {

    private final ReportService reportService;
    private final ReportPdfService reportPdfService;

    public ReportController(ReportService reportService, ReportPdfService reportPdfService) {
        this.reportService = reportService;
        this.reportPdfService = reportPdfService;
    }

    /** 查询我的报告列表 */
    @GetMapping("/mine")
    public ApiResult<?> mine() {
        return ApiResult.success(reportService.mine());
    }

    /** 查询报告详情 */
    @GetMapping("/{reportNo}")
    public ApiResult<?> detail(@PathVariable String reportNo) {
        return ApiResult.success(reportService.detail(reportNo));
    }

    /** 查询报告检查项列表 */
    @GetMapping("/{reportNo}/items")
    public ApiResult<?> items(@PathVariable String reportNo) {
        return ApiResult.success(reportService.items(reportNo));
    }

    @PostMapping("/{reportNo}/pdf")
    public ApiResult<?> generatePdf(@PathVariable String reportNo) {
        return ApiResult.success(reportPdfService.generateForOwner(reportNo, AuthContext.getUserId()));
    }

    @GetMapping("/{reportNo}/pdf/precheck")
    public ApiResult<?> precheckPdf(@PathVariable String reportNo) {
        return ApiResult.success(reportPdfService.precheckForOwner(reportNo, AuthContext.getUserId()));
    }

    @GetMapping("/{reportNo}/pdf/download")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable String reportNo) {
        ReportPdfStorageService.StoredObject stored = reportPdfService.downloadForOwner(reportNo, AuthContext.getUserId());
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
