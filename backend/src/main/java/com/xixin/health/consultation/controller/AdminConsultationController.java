package com.xixin.health.consultation.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.consultation.dto.AssignConsultationRequest;
import com.xixin.health.consultation.service.ConsultationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/doctor-consultations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConsultationController {

    private final ConsultationService consultationService;

    public AdminConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping("/{consultationNo}/assign")
    public ApiResult<?> assign(@PathVariable String consultationNo, @Validated @RequestBody AssignConsultationRequest request) {
        return ApiResult.success(consultationService.assign(consultationNo, request));
    }
}
