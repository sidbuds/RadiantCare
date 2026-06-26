package com.xixin.health.report.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.report.dto.GenerateReportRequest;
import com.xixin.health.report.dto.ReviewReportRequest;
import com.xixin.health.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端报告控制器
 */
@Deprecated
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 生成报告 */
    @PostMapping("/generate")
    public ApiResult<?> generate(@Validated @RequestBody GenerateReportRequest request) {
        return ApiResult.success(reportService.generate(request));
    }

    /** 审核报告 */
    @PostMapping("/{reportNo}/review")
    public ApiResult<?> review(@PathVariable String reportNo, @Validated @RequestBody ReviewReportRequest request) {
        return ApiResult.success(reportService.review(reportNo, request));
    }

    /** 发布报告 */
    @PostMapping("/{reportNo}/publish")
    public ApiResult<?> publish(@PathVariable String reportNo) {
        return ApiResult.success(reportService.publish(reportNo));
    }
}
