package com.xixin.health.report.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.report.dto.ReviewReportRequest;
import com.xixin.health.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生端报告控制器 - 报告审核
 */
@RestController
@RequestMapping("/api/doctor/reports")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorReportController {

    private final ReportService reportService;

    public DoctorReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 查询待审核报告 */
    @GetMapping("/review-todo")
    public ApiResult<?> reviewTodo() {
        return ApiResult.success(reportService.doctorReviewTodo());
    }

    /** 查询审核历史 */
    @GetMapping("/review-history")
    public ApiResult<?> reviewHistory() {
        return ApiResult.success(reportService.doctorReviewHistory());
    }

    /** 查询报告详情 */
    @GetMapping("/{reportNo}")
    public ApiResult<?> detail(@PathVariable String reportNo) {
        return ApiResult.success(reportService.doctorReviewDetail(reportNo));
    }

    /** 审核报告 */
    @PostMapping("/{reportNo}/review")
    public ApiResult<?> review(@PathVariable String reportNo, @Validated @RequestBody ReviewReportRequest request) {
        return ApiResult.success(reportService.doctorReview(reportNo, request));
    }
}
