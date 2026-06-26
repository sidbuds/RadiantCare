package com.xixin.health.doctor.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.doctor.service.DoctorAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 医生端数据分析控制器
 */
@RestController
@RequestMapping("/api/doctor/analytics")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAnalyticsController {

    private final DoctorAnalyticsService doctorAnalyticsService;

    public DoctorAnalyticsController(DoctorAnalyticsService doctorAnalyticsService) {
        this.doctorAnalyticsService = doctorAnalyticsService;
    }

    @GetMapping("/abnormal-overview")
    public ApiResult<?> abnormalOverview(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.abnormalOverview(startDate, endDate));
    }

    @GetMapping("/abnormal-distribution")
    public ApiResult<?> abnormalDistribution(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.abnormalDistribution(startDate, endDate));
    }

    @GetMapping("/high-risk-users")
    public ApiResult<?> highRiskUsers(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.highRiskUsers(startDate, endDate));
    }

    @GetMapping("/abnormal-trend")
    public ApiResult<?> abnormalTrend(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.abnormalTrend(startDate, endDate));
    }

    @GetMapping("/workload-overview")
    public ApiResult<?> workloadOverview(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.workloadOverview(startDate, endDate));
    }

    @GetMapping("/workload-trend")
    public ApiResult<?> workloadTrend(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.workloadTrend(startDate, endDate));
    }

    @GetMapping("/workload-breakdown")
    public ApiResult<?> workloadBreakdown(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.workloadBreakdown(startDate, endDate));
    }
}
