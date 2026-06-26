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

/**
 * 医生端体检任务控制器
 */
@RestController
@RequestMapping("/api/doctor/exam-tasks")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorExamController {

    private final ExamService examService;

    public DoctorExamController(ExamService examService) {
        this.examService = examService;
    }

    /** 查询待办检查项 */
    @GetMapping("/todo")
    public ApiResult<?> todo() {
        return ApiResult.success(examService.doctorTodo());
    }

    /** 查询检查项详情 */
    @GetMapping("/{taskNo}/items/{taskItemNo}")
    public ApiResult<?> detail(@PathVariable String taskNo, @PathVariable String taskItemNo) {
        return ApiResult.success(examService.doctorItemDetail(taskNo, taskItemNo));
    }

    /** 开始检查 */
    @PostMapping("/{taskNo}/items/{taskItemNo}/start")
    public ApiResult<Void> start(@PathVariable String taskNo, @PathVariable String taskItemNo) {
        examService.start(taskNo, taskItemNo);
        return ApiResult.success();
    }

    /** 提交检查结果 */
    @PostMapping("/{taskNo}/items/{taskItemNo}/results")
    public ApiResult<?> submit(@PathVariable String taskNo,
                               @PathVariable String taskItemNo,
                               @Validated @RequestBody SubmitExamResultRequest request) {
        return ApiResult.success(examService.submitResult(taskNo, taskItemNo, request));
    }

    /** 完成检查项 */
    @PostMapping("/{taskNo}/items/{taskItemNo}/complete")
    public ApiResult<Void> complete(@PathVariable String taskNo, @PathVariable String taskItemNo) {
        examService.completeItem(taskNo, taskItemNo);
        return ApiResult.success();
    }
}
