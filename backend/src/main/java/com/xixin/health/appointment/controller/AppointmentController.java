package com.xixin.health.appointment.controller;

import com.xixin.health.appointment.dto.CreateAppointmentRequest;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.api.ApiResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasRole('USER')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/available-slots")
    public ApiResult<List<ResourceCapacityEntity>> availableSlots(@RequestParam String centerCode,
                                                                 @RequestParam Long packageId,
                                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ApiResult.success(appointmentService.availableSlots(centerCode, packageId, date));
    }

    @PostMapping
    public ApiResult<Map<String, Object>> create(@Validated @RequestBody CreateAppointmentRequest request) {
        return ApiResult.success(appointmentService.create(request));
    }

    @GetMapping("/{appointmentNo}")
    public ApiResult<?> detail(@PathVariable String appointmentNo) {
        return ApiResult.success(appointmentService.detail(appointmentNo));
    }

    @GetMapping("/mine")
    public ApiResult<?> mine() {
        return ApiResult.success(appointmentService.mine());
    }

    @PostMapping("/{appointmentNo}/cancel")
    public ApiResult<Void> cancel(@PathVariable String appointmentNo, @RequestParam(required = false) String reason) {
        appointmentService.cancel(appointmentNo, reason);
        return ApiResult.success();
    }
}
