package com.xixin.health.exam.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.exam.dto.GenerateExamTaskRequest;
import com.xixin.health.exam.service.ExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/exam-tasks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExamController {

    private final ExamService examService;

    public AdminExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/generate")
    public ApiResult<?> generate(@Validated @RequestBody GenerateExamTaskRequest request) {
        return ApiResult.success(examService.generateTask(request));
    }
}
