package com.xixin.health.exam.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.exam.service.ExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exam-tasks")
@PreAuthorize("hasRole('USER')")
public class ExamTaskController {

    private final ExamService examService;

    public ExamTaskController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/mine/current")
    public ApiResult<?> currentTask() {
        return ApiResult.success(examService.currentTask());
    }

    @GetMapping("/{taskNo}")
    public ApiResult<?> detail(@PathVariable String taskNo) {
        return ApiResult.success(examService.detail(taskNo));
    }

    @GetMapping("/{taskNo}/guide")
    public ApiResult<?> guide(@PathVariable String taskNo) {
        return ApiResult.success(examService.guide(taskNo));
    }

    @GetMapping("/{taskNo}/items")
    public ApiResult<?> items(@PathVariable String taskNo) {
        return ApiResult.success(examService.taskItemsForUser(taskNo));
    }
}
