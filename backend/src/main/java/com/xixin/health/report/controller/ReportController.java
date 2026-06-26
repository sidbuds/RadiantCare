package com.xixin.health.report.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报告控制器 - 用户查看体检报告
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('USER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
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
}
