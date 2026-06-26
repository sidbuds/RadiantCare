package com.xixin.health.compare.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.compare.dto.CreateCompareTaskRequest;
import com.xixin.health.compare.service.CompareService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报告对比控制器 - 用户对比体检报告
 */
@RestController
@RequestMapping("/api/report-compare/tasks")
@PreAuthorize("hasRole('USER')")
public class ReportCompareController {

    private final CompareService compareService;

    public ReportCompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    /** 创建对比任务 */
    @PostMapping
    public ApiResult<?> create(@Validated @RequestBody CreateCompareTaskRequest request) {
        return ApiResult.success(compareService.createTask(request));
    }

    /** 查询对比任务详情 */
    @GetMapping("/{taskNo}")
    public ApiResult<?> detail(@PathVariable String taskNo) {
        return ApiResult.success(compareService.taskDetail(taskNo));
    }

    /** 查询对比结果 */
    @GetMapping("/{taskNo}/results")
    public ApiResult<?> results(@PathVariable String taskNo) {
        return ApiResult.success(compareService.taskResults(taskNo));
    }

    /** 导出对比结果 */
    @GetMapping("/{taskNo}/export")
    public ApiResult<?> export(@PathVariable String taskNo) {
        return ApiResult.success(compareService.export(taskNo));
    }
}
