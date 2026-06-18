package com.xixin.health.consultation.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.consultation.dto.CreateConsultationRequest;
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
@RequestMapping("/api/doctor-consultations")
@PreAuthorize("hasRole('USER')")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public ApiResult<?> create(@Validated @RequestBody CreateConsultationRequest request) {
        return ApiResult.success(consultationService.create(request));
    }

    @GetMapping("/mine")
    public ApiResult<?> mine() {
        return ApiResult.success(consultationService.mine());
    }

    @GetMapping("/{consultationNo}")
    public ApiResult<?> detail(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.userDetail(consultationNo));
    }

    @PostMapping("/{consultationNo}/close")
    public ApiResult<?> close(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.closeByUser(consultationNo));
    }
}
