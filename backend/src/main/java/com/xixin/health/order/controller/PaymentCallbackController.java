package com.xixin.health.order.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.order.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付回调接口
 * 生产环境需根据微信/支付宝文档实现签名验证
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentCallbackController {

    private final PaymentService paymentService;

    public PaymentCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/notify")
    public ApiResult<?> notify(@RequestBody Map<String, String> callbackData) {
        log.info("收到支付回调: {}", callbackData);
        boolean success = paymentService.handleCallback(callbackData);
        if (success) {
            return ApiResult.success("回调处理成功");
        } else {
            return ApiResult.fail(4001, "回调处理失败");
        }
    }
}
