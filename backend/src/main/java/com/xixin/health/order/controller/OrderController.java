package com.xixin.health.order.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.order.dto.ApplyRefundRequest;
import com.xixin.health.order.dto.CreateOrderRequest;
import com.xixin.health.order.service.OrderService;
import com.xixin.health.order.service.RefundService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单控制器 - 用户订单管理
 */
@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;
    private final RefundService refundService;

    public OrderController(OrderService orderService, RefundService refundService) {
        this.orderService = orderService;
        this.refundService = refundService;
    }

    /** 创建订单 */
    @PostMapping
    public ApiResult<Map<String, Object>> create(@Validated @RequestBody CreateOrderRequest request) {
        return ApiResult.success(orderService.create(request));
    }

    /** 查询订单详情 */
    @GetMapping("/{orderNo}")
    public ApiResult<?> detail(@PathVariable String orderNo) {
        return ApiResult.success(orderService.detail(orderNo));
    }

    /** 查询我的订单列表 */
    @GetMapping("/mine")
    public ApiResult<?> mine() {
        return ApiResult.success(orderService.mine());
    }

    /** 订单支付 */
    @PostMapping("/{orderNo}/pay")
    public ApiResult<Map<String, Object>> pay(@PathVariable String orderNo) {
        return ApiResult.success(orderService.pay(orderNo));
    }

    /** 申请退款 */
    @PostMapping("/{orderNo}/refund")
    public ApiResult<?> refund(@PathVariable String orderNo, @Validated @RequestBody ApplyRefundRequest request) {
        return ApiResult.success(refundService.apply(orderNo, request));
    }
}