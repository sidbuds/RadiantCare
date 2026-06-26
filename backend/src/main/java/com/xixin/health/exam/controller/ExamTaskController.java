package com.xixin.health.exam.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.exam.service.ExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 体检任务控制器 - 用户查看体检任务
 */
@RestController
@RequestMapping("/api/exam-tasks")
@PreAuthorize("hasRole('USER')")
public class ExamTaskController {

    private final ExamService examService;

    public ExamTaskController(ExamService examService) {
        this.examService = examService;
    }

    /** 查询当前用户的最新体检任务 */
    @GetMapping("/mine/current")
    public ApiResult<?> currentTask() {
        return ApiResult.success(examService.currentTask());
    }

    /** 查询任务详情 */
    @GetMapping("/{taskNo}")
    public ApiResult<?> detail(@PathVariable String taskNo) {
        return ApiResult.success(examService.detail(taskNo));
    }

    /** 查询体检导检单 */
    @GetMapping("/{taskNo}/guide")
    public ApiResult<?> guide(@PathVariable String taskNo) {
        return ApiResult.success(examService.guide(taskNo));
    }

    /** 查询任务检查项列表 */
    @GetMapping("/{taskNo}/items")
    public ApiResult<?> items(@PathVariable String taskNo) {
        return ApiResult.success(examService.taskItemsForUser(taskNo));
    }
}
