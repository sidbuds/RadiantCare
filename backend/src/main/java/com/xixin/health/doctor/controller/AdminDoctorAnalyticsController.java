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
 * 管理端医生数据分析控制器
 */
@Deprecated
@RestController
@RequestMapping("/api/admin/doctor-analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorAnalyticsController {

    private final DoctorAnalyticsService doctorAnalyticsService;

    public AdminDoctorAnalyticsController(DoctorAnalyticsService doctorAnalyticsService) {
        this.doctorAnalyticsService = doctorAnalyticsService;
    }

    @GetMapping("/abnormal-overview")
    public ApiResult<?> abnormalOverview(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                         @RequestParam(required = false) Long doctorId,
                                         @RequestParam(required = false) String departmentCode) {
        return ApiResult.success(doctorAnalyticsService.abnormalOverviewForAdmin(startDate, endDate, doctorId, departmentCode));
    }

    @GetMapping("/abnormal-distribution")
    public ApiResult<?> abnormalDistribution(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                             @RequestParam(required = false) Long doctorId,
                                             @RequestParam(required = false) String departmentCode) {
        return ApiResult.success(doctorAnalyticsService.abnormalDistributionForAdmin(startDate, endDate, doctorId, departmentCode));
    }

    @GetMapping("/high-risk-users")
    public ApiResult<?> highRiskUsers(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                      @RequestParam(required = false) Long doctorId,
                                      @RequestParam(required = false) String departmentCode) {
        return ApiResult.success(doctorAnalyticsService.highRiskUsersForAdmin(startDate, endDate, doctorId, departmentCode));
    }

    @GetMapping("/workload-ranking")
    public ApiResult<?> workloadRanking(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                        @RequestParam(required = false) String departmentCode) {
        return ApiResult.success(doctorAnalyticsService.workloadRanking(startDate, endDate, departmentCode));
    }

    @GetMapping("/department-workload")
    public ApiResult<?> departmentWorkload(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(doctorAnalyticsService.departmentWorkload(startDate, endDate));
    }
}
