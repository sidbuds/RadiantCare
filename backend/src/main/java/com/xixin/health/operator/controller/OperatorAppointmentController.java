package com.xixin.health.operator.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.operator.service.OperatorAppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/operator/appointments")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorAppointmentController {

    private final OperatorAppointmentService operatorAppointmentService;

    public OperatorAppointmentController(OperatorAppointmentService operatorAppointmentService) {
        this.operatorAppointmentService = operatorAppointmentService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) String centerCode,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate appointDate,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) String mobile) {
        return ApiResult.success(operatorAppointmentService.list(centerCode, appointDate, status, mobile));
    }

    @GetMapping("/{appointmentNo}")
    public ApiResult<?> detail(@PathVariable String appointmentNo) {
        return ApiResult.success(operatorAppointmentService.detail(appointmentNo));
    }

    @PostMapping("/{appointmentNo}/cancel")
    public ApiResult<?> cancel(@PathVariable String appointmentNo,
                               @RequestParam(required = false) String reason) {
        operatorAppointmentService.cancel(appointmentNo, reason);
        return ApiResult.success();
    }
}
