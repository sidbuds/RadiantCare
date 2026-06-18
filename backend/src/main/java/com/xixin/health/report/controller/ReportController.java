package com.xixin.health.report.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('USER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/mine")
    public ApiResult<?> mine() {
        return ApiResult.success(reportService.mine());
    }

    @GetMapping("/{reportNo}")
    public ApiResult<?> detail(@PathVariable String reportNo) {
        return ApiResult.success(reportService.detail(reportNo));
    }

    @GetMapping("/{reportNo}/items")
    public ApiResult<?> items(@PathVariable String reportNo) {
        return ApiResult.success(reportService.items(reportNo));
    }
}
