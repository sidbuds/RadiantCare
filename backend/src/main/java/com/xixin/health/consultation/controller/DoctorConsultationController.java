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

@RestController
@RequestMapping("/api/doctor/consultations")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorConsultationController {

    private final ConsultationService consultationService;

    public DoctorConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/todo")
    public ApiResult<?> todo() {
        return ApiResult.success(consultationService.doctorTodo());
    }

    @GetMapping("/{consultationNo}")
    public ApiResult<?> detail(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.doctorDetail(consultationNo));
    }

    @PostMapping("/{consultationNo}/reply")
    public ApiResult<?> reply(@PathVariable String consultationNo, @Validated @RequestBody ReplyConsultationRequest request) {
        return ApiResult.success(consultationService.reply(consultationNo, request));
    }
}
