package com.xixin.health.operator.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.operator.service.OperatorOptionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operator/options")
@PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
public class OperatorOptionController {

    private final OperatorOptionService operatorOptionService;

    public OperatorOptionController(OperatorOptionService operatorOptionService) {
        this.operatorOptionService = operatorOptionService;
    }

    @GetMapping("/centers")
    public ApiResult<?> centers() {
        return ApiResult.success(operatorOptionService.centers());
    }

    @GetMapping("/departments")
    public ApiResult<?> departments(@RequestParam(required = false) String centerCode) {
        return ApiResult.success(operatorOptionService.departments(centerCode));
    }

    @GetMapping("/exam-items")
    public ApiResult<?> examItems() {
        return ApiResult.success(operatorOptionService.examItems());
    }

    @GetMapping("/time-slots")
    public ApiResult<?> timeSlots(@RequestParam(required = false) String centerCode,
                                  @RequestParam(required = false) String departmentCode) {
        return ApiResult.success(operatorOptionService.timeSlots(centerCode, departmentCode));
    }
}
