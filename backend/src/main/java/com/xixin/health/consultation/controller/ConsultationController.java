package com.xixin.health.consultation.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.consultation.dto.CreateConsultationRequest;
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
 * 咨询控制器 - 用户发起医生咨询
 */
@RestController
@RequestMapping("/api/doctor-consultations")
@PreAuthorize("hasRole('USER')")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /** 创建咨询 */
    @PostMapping
    public ApiResult<?> create(@Validated @RequestBody CreateConsultationRequest request) {
        return ApiResult.success(consultationService.create(request));
    }

    /** 查询我的咨询列表 */
    @GetMapping("/mine")
    public ApiResult<?> mine() {
        return ApiResult.success(consultationService.mine());
    }

    /** 查询咨询详情 */
    @GetMapping("/{consultationNo}")
    public ApiResult<?> detail(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.userDetail(consultationNo));
    }

    /** 发送消息 */
    @PostMapping("/{consultationNo}/messages")
    public ApiResult<?> sendMessage(@PathVariable String consultationNo, @Validated @RequestBody ReplyConsultationRequest request) {
        return ApiResult.success(consultationService.userMessage(consultationNo, request));
    }

    /** 关闭咨询 */
    @PostMapping("/{consultationNo}/close")
    public ApiResult<?> close(@PathVariable String consultationNo) {
        return ApiResult.success(consultationService.closeByUser(consultationNo));
    }
}
