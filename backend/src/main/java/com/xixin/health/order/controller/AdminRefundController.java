package com.xixin.health.order.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.order.dto.RefundAuditRequest;
import com.xixin.health.order.service.RefundService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/refunds")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {

    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) Integer applyStatus,
                             @RequestParam(required = false) String orderNo) {
        return ApiResult.success(refundService.list(applyStatus, orderNo));
    }

    @GetMapping("/{applyNo}")
    public ApiResult<?> detail(@PathVariable String applyNo) {
        return ApiResult.success(refundService.detail(applyNo));
    }

    @PostMapping("/{applyNo}/approve")
    public ApiResult<?> approve(@PathVariable String applyNo,
                                @Validated @RequestBody RefundAuditRequest request) {
        return ApiResult.success(refundService.approve(applyNo, request));
    }

    @PostMapping("/{applyNo}/reject")
    public ApiResult<?> reject(@PathVariable String applyNo,
                               @Validated @RequestBody RefundAuditRequest request) {
        return ApiResult.success(refundService.reject(applyNo, request));
    }
}