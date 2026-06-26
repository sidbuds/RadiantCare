package com.xixin.health.order.service;

import com.xixin.health.order.entity.OrderEntity;

import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 发起支付
     * @param order 订单
     * @return 支付参数（如支付链接、二维码等）
     */
    Map<String, Object> createPayment(OrderEntity order);

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态信息
     */
    Map<String, Object> queryPayment(String orderNo);

    /**
     * 处理支付回调
     * @param callbackData 回调数据
     * @return 处理结果
     */
    boolean handleCallback(Map<String, String> callbackData);

    void handlePaymentSuccess(String orderNo, String transactionNo, String payChannel);
}
