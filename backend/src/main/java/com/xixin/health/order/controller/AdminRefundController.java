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

/**
 * 管理端退款控制器
 */
@Deprecated
@RestController
@RequestMapping("/api/admin/refunds")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {

    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    /** 查询退款申请列表 */
    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) Integer applyStatus,
                             @RequestParam(required = false) String orderNo) {
        return ApiResult.success(refundService.list(applyStatus, orderNo));
    }

    /** 查询退款申请详情 */
    @GetMapping("/{applyNo}")
    public ApiResult<?> detail(@PathVariable String applyNo) {
        return ApiResult.success(refundService.detail(applyNo));
    }

    /** 审批通过退款 */
    @PostMapping("/{applyNo}/approve")
    public ApiResult<?> approve(@PathVariable String applyNo,
                                @Validated @RequestBody RefundAuditRequest request) {
        return ApiResult.success(refundService.approve(applyNo, request));
    }

    /** 驳回退款申请 */
    @PostMapping("/{applyNo}/reject")
    public ApiResult<?> reject(@PathVariable String applyNo,
                               @Validated @RequestBody RefundAuditRequest request) {
        return ApiResult.success(refundService.reject(applyNo, request));
    }
}