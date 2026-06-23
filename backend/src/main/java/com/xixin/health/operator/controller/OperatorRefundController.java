package com.xixin.health.operator.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xixin.health.common.api.ApiResult;
import com.xixin.health.common.model.PageResult;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operator/refunds")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorRefundController {

    private final RefundService refundService;

    public OperatorRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) Integer applyStatus,
                             @RequestParam(required = false) String orderNo,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<?> list = refundService.list(applyStatus, orderNo);
        PageInfo<?> pageInfo = new PageInfo<>(list);
        return ApiResult.success(PageResult.of(list, pageInfo.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{applyNo}")
    public ApiResult<?> detail(@PathVariable String applyNo) {
        return ApiResult.success(refundService.detail(applyNo));
    }

    @PostMapping("/{applyNo}/approve")
    public ApiResult<?> approve(@PathVariable String applyNo, @Validated @RequestBody RefundAuditRequest request) {
        return ApiResult.success(refundService.approve(applyNo, request));
    }

    @PostMapping("/{applyNo}/reject")
    public ApiResult<?> reject(@PathVariable String applyNo, @Validated @RequestBody RefundAuditRequest request) {
        return ApiResult.success(refundService.reject(applyNo, request));
    }
}
