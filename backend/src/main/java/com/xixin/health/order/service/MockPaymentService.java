package com.xixin.health.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.PaymentRecordEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.mapper.PaymentRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MockPaymentService implements PaymentService {

    private static final String PAY_CHANNEL = "MOCK_SANDBOX";

    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public MockPaymentService(OrderMapper orderMapper, PaymentRecordMapper paymentRecordMapper) {
        this.orderMapper = orderMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @Override
    public Map<String, Object> createPayment(OrderEntity order) {
        String transactionNo = "MOCK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        PaymentRecordEntity record = new PaymentRecordEntity();
        record.setPaymentNo("PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        record.setOrderNo(order.getOrderNo());
        record.setUserId(order.getUserId());
        record.setPayMethod(PAY_CHANNEL);
        record.setThirdPartyNo(transactionNo);
        record.setPayAmount(order.getPayAmount());
        record.setPayStatus(0);
        record.setRemark("沙箱环境模拟支付");
        record.setIsDeleted(0);
        paymentRecordMapper.insert(record);

        log.info("Mock payment created: orderNo={}, transactionNo={}, amount={}",
                order.getOrderNo(), transactionNo, order.getPayAmount());

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("transactionNo", transactionNo);
        result.put("payChannel", PAY_CHANNEL);
        result.put("payUrl", "mock://pay/" + transactionNo);
        result.put("expireTime", LocalDateTime.now().plusMinutes(30).toString());
        return result;
    }

    @Override
    public Map<String, Object> queryPayment(String orderNo) {
        PaymentRecordEntity record = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecordEntity>()
                        .eq(PaymentRecordEntity::getOrderNo, orderNo)
                        .eq(PaymentRecordEntity::getIsDeleted, 0)
                        .orderByDesc(PaymentRecordEntity::getId)
                        .last("limit 1"));

        Map<String, Object> result = new HashMap<String, Object>();
        if (record != null) {
            result.put("transactionNo", record.getThirdPartyNo());
            result.put("payStatus", record.getPayStatus());
            result.put("payChannel", record.getPayMethod());
            result.put("payTime", record.getPayTime());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean handleCallback(Map<String, String> callbackData) {
        String transactionNo = callbackData.get("transactionNo");
        String orderNo = callbackData.get("orderNo");
        String status = callbackData.get("status");

        if (!"SUCCESS".equals(status)) {
            log.warn("Payment callback failed status: transactionNo={}, status={}", transactionNo, status);
            return false;
        }

        PaymentRecordEntity record = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecordEntity>()
                        .eq(PaymentRecordEntity::getThirdPartyNo, transactionNo)
                        .eq(PaymentRecordEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (record == null) {
            log.error("Payment record not found: transactionNo={}", transactionNo);
            return false;
        }
        if (!record.getOrderNo().equals(orderNo)) {
            log.error("Payment callback order mismatch: recordOrderNo={}, callbackOrderNo={}", record.getOrderNo(), orderNo);
            return false;
        }
        if (Integer.valueOf(1).equals(record.getPayStatus())) {
            log.info("Payment callback already handled: orderNo={}, transactionNo={}", orderNo, transactionNo);
            return true;
        }

        int paymentUpdated = paymentRecordMapper.update(null, new LambdaUpdateWrapper<PaymentRecordEntity>()
                .eq(PaymentRecordEntity::getId, record.getId())
                .eq(PaymentRecordEntity::getPayStatus, 0)
                .set(PaymentRecordEntity::getPayStatus, 1)
                .set(PaymentRecordEntity::getPayTime, LocalDateTime.now()));

        int orderUpdated = orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getStatus, 0)
                .set(OrderEntity::getStatus, 1)
                .set(OrderEntity::getPayChannel, PAY_CHANNEL)
                .set(OrderEntity::getPayTime, LocalDateTime.now()));

        boolean success = paymentUpdated > 0 && orderUpdated > 0;
        log.info("Payment callback handled: orderNo={}, transactionNo={}, success={}", orderNo, transactionNo, success);
        return success;
    }
}
