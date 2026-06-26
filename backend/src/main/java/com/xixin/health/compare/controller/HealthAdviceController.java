package com.xixin.health.compare.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.compare.service.CompareService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康建议控制器
 */
@RestController
@RequestMapping("/api/health-advices")
@PreAuthorize("hasRole('USER')")
public class HealthAdviceController {

    private final CompareService compareService;

    public HealthAdviceController(CompareService compareService) {
        this.compareService = compareService;
    }

    /** 查询对比任务的健康建议 */
    @GetMapping("/compare-tasks/{taskNo}")
    public ApiResult<?> compareTaskAdvices(@PathVariable String taskNo) {
        return ApiResult.success(compareService.healthAdvices(taskNo));
    }
}
