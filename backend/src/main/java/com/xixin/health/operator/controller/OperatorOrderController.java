package com.xixin.health.operator.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.operator.service.OperatorOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operator/orders")
@PreAuthorize("hasRole('OPERATOR')")
public class OperatorOrderController {

    private final OperatorOrderService operatorOrderService;

    public OperatorOrderController(OperatorOrderService operatorOrderService) {
        this.operatorOrderService = operatorOrderService;
    }

    @GetMapping
    public ApiResult<?> list(@RequestParam(required = false) String orderNo,
                             @RequestParam(required = false) Long userId,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) String payChannel) {
        return ApiResult.success(operatorOrderService.list(orderNo, userId, status, payChannel));
    }

    @GetMapping("/{orderNo}")
    public ApiResult<?> detail(@PathVariable String orderNo) {
        return ApiResult.success(operatorOrderService.detail(orderNo));
    }

    @GetMapping("/export")
    public ApiResult<?> export(@RequestParam(required = false) String orderNo,
                               @RequestParam(required = false) Long userId,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) String payChannel) {
        return ApiResult.success(operatorOrderService.export(orderNo, userId, status, payChannel));
    }
}
