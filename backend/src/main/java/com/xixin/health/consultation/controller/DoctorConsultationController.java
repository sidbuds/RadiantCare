package com.xixin.health.consultation.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.consultation.dto.ReplyConsultationRequest;
import com.xixin.health.consultation.service.ConsultationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生端咨询控制器
 */
@RestController
@RequestMapping("/api/doctor/consultations")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorConsultationController {

    private final ConsultationService consultationService;

    public DoctorConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /** 查询待处理咨询 */
    @GetMapping("/todo")
    public ApiResult<?> todo() {
        return ApiResult.success(consultationService.doctorTodo());
    }

    /** 查询咨询详情 */
    @GetMapping("/{consultationNo}")
    public ApiResult<?> detail(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.doctorDetail(consultationNo));
    }

    /** 回复咨询 */
    @PostMapping("/{consultationNo}/reply")
    public ApiResult<?> reply(@PathVariable String consultationNo, @Validated @RequestBody ReplyConsultationRequest request) {
        return ApiResult.success(consultationService.reply(consultationNo, request));
    }
}
