package com.xixin.health.operator.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.operator.dto.SavePackageRequest;
import com.xixin.health.operator.service.OperatorPackageService;
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

@RestController
@RequestMapping("/api/operator/packages")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorPackageController {

    private final OperatorPackageService operatorPackageService;

    public OperatorPackageController(OperatorPackageService operatorPackageService) {
        this.operatorPackageService = operatorPackageService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) String packageName,
                             @RequestParam(required = false) Integer status) {
        return ApiResult.success(operatorPackageService.list(packageName, status));
    }

    @GetMapping("/{id}")
    public ApiResult<?> detail(@PathVariable Long id) {
        return ApiResult.success(operatorPackageService.detail(id));
    }

    @PostMapping
    public ApiResult<?> create(@Validated @RequestBody SavePackageRequest request) {
        return ApiResult.success(operatorPackageService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<?> update(@PathVariable Long id, @Validated @RequestBody SavePackageRequest request) {
        return ApiResult.success(operatorPackageService.update(id, request));
    }
}
