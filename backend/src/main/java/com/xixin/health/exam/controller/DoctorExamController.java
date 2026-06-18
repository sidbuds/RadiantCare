package com.xixin.health.exam.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.exam.dto.SubmitExamResultRequest;
import com.xixin.health.exam.service.ExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor/exam-tasks")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorExamController {

    private final ExamService examService;

    public DoctorExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/todo")
    public ApiResult<?> todo() {
        return ApiResult.success(examService.doctorTodo());
    }

    @GetMapping("/{taskNo}/items/{taskItemNo}")
    public ApiResult<?> detail(@PathVariable String taskNo, @PathVariable String taskItemNo) {
        return ApiResult.success(examService.doctorItemDetail(taskNo, taskItemNo));
    }

    @PostMapping("/{taskNo}/items/{taskItemNo}/start")
    public ApiResult<Void> start(@PathVariable String taskNo, @PathVariable String taskItemNo) {
        examService.start(taskNo, taskItemNo);
        return ApiResult.success();
    }

    @PostMapping("/{taskNo}/items/{taskItemNo}/results")
    public ApiResult<?> submit(@PathVariable String taskNo,
                               @PathVariable String taskItemNo,
                               @Validated @RequestBody SubmitExamResultRequest request) {
        return ApiResult.success(examService.submitResult(taskNo, taskItemNo, request));
    }

    @PostMapping("/{taskNo}/items/{taskItemNo}/complete")
    public ApiResult<Void> complete(@PathVariable String taskNo, @PathVariable String taskItemNo) {
        examService.completeItem(taskNo, taskItemNo);
        return ApiResult.success();
    }
}
