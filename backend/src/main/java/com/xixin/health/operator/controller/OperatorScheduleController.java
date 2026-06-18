package com.xixin.health.operator.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.operator.dto.SaveScheduleRequest;
import com.xixin.health.operator.service.OperatorScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/operator/schedules")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorScheduleController {

    private final OperatorScheduleService operatorScheduleService;

    public OperatorScheduleController(OperatorScheduleService operatorScheduleService) {
        this.operatorScheduleService = operatorScheduleService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) String centerCode,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResult.success(operatorScheduleService.list(centerCode, startDate, endDate));
    }

    @PostMapping
    public ApiResult<?> create(@Validated @RequestBody SaveScheduleRequest request) {
        return ApiResult.success(operatorScheduleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<?> update(@PathVariable Long id, @Validated @RequestBody SaveScheduleRequest request) {
        return ApiResult.success(operatorScheduleService.update(id, request));
    }
}
