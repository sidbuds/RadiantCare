package com.xixin.health.operator.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.operator.service.OperatorAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 运营端数据分析控制器
 */
@RestController
@RequestMapping("/api/operator/analytics")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorAnalyticsController {

    private final OperatorAnalyticsService operatorAnalyticsService;

    public OperatorAnalyticsController(OperatorAnalyticsService operatorAnalyticsService) {
        this.operatorAnalyticsService = operatorAnalyticsService;
    }

    @GetMapping("/dashboard")
    public ApiResult<?> dashboard(@RequestParam(required = false) String departmentCode,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(operatorAnalyticsService.dashboard(departmentCode, startDate, endDate));
    }

    @GetMapping("/appointment-trend")
    public ApiResult<?> appointmentTrend(@RequestParam(required = false) String departmentCode,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(operatorAnalyticsService.appointmentTrend(departmentCode, startDate, endDate));
    }

    @GetMapping("/order-conversion")
    public ApiResult<?> orderConversion(@RequestParam(required = false) String departmentCode,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(operatorAnalyticsService.orderConversion(departmentCode, startDate, endDate));
    }

    @GetMapping("/package-analysis")
    public ApiResult<?> packageAnalysis(@RequestParam(required = false) String departmentCode,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(operatorAnalyticsService.packageAnalysis(departmentCode, startDate, endDate));
    }
}
