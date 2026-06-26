package com.xixin.health.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.exam.service.ExamService;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.PaymentRecordEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.mapper.PaymentRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 模拟支付服务 - 沙箱环境使用
 */
@Slf4j
@Service
public class MockPaymentService implements PaymentService {

    private static final String PAY_CHANNEL = "MOCK_SANDBOX";

    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final ExamService examService;

    public MockPaymentService(OrderMapper orderMapper,
                              PaymentRecordMapper paymentRecordMapper,
                              AppointmentMapper appointmentMapper,
                              ExamService examService) {
        this.orderMapper = orderMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.appointmentMapper = appointmentMapper;
        this.examService = examService;
    }

    /** 创建模拟支付 */
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

    /** 查询支付状态 */
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

    /** 处理支付回调 */
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
        handlePaymentSuccess(orderNo, transactionNo, PAY_CHANNEL);
        log.info("Payment callback handled: orderNo={}, transactionNo={}", orderNo, transactionNo);
        return true;
    }

    /** 处理支付成功后续逻辑 */
    @Transactional
    public void handlePaymentSuccess(String orderNo, String transactionNo, String payChannel) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (order == null) {
            log.error("Payment success order not found: orderNo={}", orderNo);
            return;
        }

        PaymentRecordEntity record = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecordEntity>()
                .eq(PaymentRecordEntity::getOrderNo, orderNo)
                .eq(transactionNo != null && transactionNo.length() > 0, PaymentRecordEntity::getThirdPartyNo, transactionNo)
                .eq(PaymentRecordEntity::getIsDeleted, 0)
                .orderByDesc(PaymentRecordEntity::getId)
                .last("limit 1"));
        if (record != null && !Integer.valueOf(1).equals(record.getPayStatus())) {
            paymentRecordMapper.update(null, new LambdaUpdateWrapper<PaymentRecordEntity>()
                    .eq(PaymentRecordEntity::getId, record.getId())
                    .set(PaymentRecordEntity::getPayStatus, 1)
                    .set(PaymentRecordEntity::getPayTime, LocalDateTime.now()));
        }

        if (!Integer.valueOf(1).equals(order.getStatus())) {
            orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                    .eq(OrderEntity::getId, order.getId())
                    .set(OrderEntity::getStatus, 1)
                    .set(OrderEntity::getPayChannel, payChannel)
                    .set(OrderEntity::getPayTime, LocalDateTime.now()));
            order.setStatus(1);
            order.setPayChannel(payChannel);
            order.setPayTime(LocalDateTime.now());
        }

        appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getId, order.getAppointmentId())
                .in(AppointmentEntity::getStatus, 0, 1)
                .set(AppointmentEntity::getStatus, 1));

        triggerTaskGenerationAfterCommit(order);
    }

    private void triggerTaskGenerationAfterCommit(final OrderEntity order) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    autoGenerateTask(order);
                }
            });
        } else {
            autoGenerateTask(order);
        }
    }

    private void autoGenerateTask(OrderEntity order) {
        try {
            examService.generateTaskForPaidOrder(order);
        } catch (Exception e) {
            log.error("Auto generate exam task failed: orderNo={}", order.getOrderNo(), e);
        }
    }
}
