package com.xixin.health.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.SystemConfigService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.order.dto.CreateOrderRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.OrderItemEntity;
import com.xixin.health.order.mapper.OrderItemMapper;
import com.xixin.health.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务 - 处理订单创建/支付/查询
 */
@Slf4j
@Service
public class OrderService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_REFUNDED = 2;
    public static final int STATUS_REFUNDING = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELED = 5;

    public static final String TIMEOUT_KEY_PREFIX = "order:pay:timeout:";
    private static final String AUTO_CANCEL_REASON = "订单超时未支付自动取消";
    private static final String USER_CANCEL_REASON = "用户主动取消订单";
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AppointmentService appointmentService;
    private final ExamPackageMapper examPackageMapper;
    private final PaymentService paymentService;
    private final SystemConfigService systemConfigService;
    private final StringRedisTemplate redisTemplate;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        AppointmentService appointmentService,
                        ExamPackageMapper examPackageMapper,
                        PaymentService paymentService,
                        SystemConfigService systemConfigService,
                        StringRedisTemplate redisTemplate) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.appointmentService = appointmentService;
        this.examPackageMapper = examPackageMapper;
        this.paymentService = paymentService;
        this.systemConfigService = systemConfigService;
        this.redisTemplate = redisTemplate;
    }

    /** 创建订单 */
    @Transactional
    public Map<String, Object> create(CreateOrderRequest request) {
        AppointmentEntity appointment = appointmentService.getByNo(request.getAppointmentNo());
        if (!appointment.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        long count = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getAppointmentId, appointment.getId())
                .eq(OrderEntity::getIsDeleted, 0));
        if (count > 0) {
            OrderEntity existing = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                    .eq(OrderEntity::getAppointmentId, appointment.getId())
                    .eq(OrderEntity::getIsDeleted, 0)
                    .last("limit 1"));
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", existing.getOrderNo());
            result.put("status", getOrderStatusStr(existing.getStatus()));
            result.put("totalAmount", existing.getTotalAmount());
            result.put("isExisting", true);
            fillExpireInfo(result, existing);
            return result;
        }
        ExamPackageEntity packageEntity = examPackageMapper.selectById(appointment.getPackageId());
        if (packageEntity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "套餐不存在");
        }

        OrderEntity order = new OrderEntity();
        order.setOrderNo(NoGenerator.next("ORD"));
        order.setAppointmentId(appointment.getId());
        order.setUserId(appointment.getUserId());
        order.setPackageId(appointment.getPackageId());
        order.setTotalAmount(packageEntity.getPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(packageEntity.getPrice());
        order.setStatus(STATUS_PENDING);
        order.setIsDeleted(0);
        orderMapper.insert(order);

        OrderItemEntity packageItem = new OrderItemEntity();
        packageItem.setOrderId(order.getId());
        packageItem.setItemType(0);
        packageItem.setRefItemCode(packageEntity.getPackageCode());
        packageItem.setRefItemName(packageEntity.getPackageName());
        packageItem.setQty(1);
        packageItem.setUnitPrice(packageEntity.getPrice());
        packageItem.setAmount(packageEntity.getPrice());
        packageItem.setIsDeleted(0);
        orderItemMapper.insert(packageItem);

        if (request.getExtraItemCodes() != null) {
            for (String extraItemCode : request.getExtraItemCodes()) {
                OrderItemEntity extraItem = new OrderItemEntity();
                extraItem.setOrderId(order.getId());
                extraItem.setItemType(1);
                extraItem.setRefItemCode(extraItemCode);
                extraItem.setRefItemName(extraItemCode);
                extraItem.setQty(1);
                extraItem.setUnitPrice(BigDecimal.ZERO);
                extraItem.setAmount(BigDecimal.ZERO);
                extraItem.setIsDeleted(0);
                orderItemMapper.insert(extraItem);
            }
        }

        log.info("Order created: orderNo={}, userId={}, amount={}", order.getOrderNo(), order.getUserId(), order.getPayAmount());

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", order.getTotalAmount());
        result.put("discountAmount", order.getDiscountAmount());
        result.put("payAmount", order.getPayAmount());
        result.put("status", "PENDING");
        fillExpireInfo(result, order);
        registerPaymentTimeout(order);
        return result;
    }

    /** 查询订单详情 */
    public Map<String, Object> detail(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        if (!order.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        cancelIfExpired(order);
        return buildOrderResponse(order);
    }

    /** 查询当前用户订单列表 */
    public List<OrderEntity> mine() {
        return orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, AuthContext.getUserId())
                .eq(OrderEntity::getIsDeleted, 0)
                .orderByDesc(OrderEntity::getId));
    }

    /** 订单支付 */
    @Transactional
    public Map<String, Object> pay(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        if (!order.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (cancelIfExpired(order)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (order.getStatus() == null || order.getStatus() != STATUS_PENDING) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        Map<String, Object> payResult = paymentService.createPayment(order);
        log.info("Payment created: orderNo={}, transactionNo={}", orderNo, payResult.get("transactionNo"));

        paymentService.handlePaymentSuccess(orderNo, (String) payResult.get("transactionNo"),
                (String) payResult.get("payChannel"));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("orderNo", orderNo);
        result.put("status", "PAID");
        return result;
    }

    @Transactional
    public Map<String, Object> cancelByUser(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        if (!order.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() == null || order.getStatus() != STATUS_PENDING) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        cancelPendingOrder(order, USER_CANCEL_REASON);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("orderNo", orderNo);
        result.put("status", "CANCELED");
        return result;
    }

    public OrderEntity getByNo(String orderNo) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getIsDeleted, 0));
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    @Transactional
    public boolean cancelExpiredPendingOrder(String orderNo) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (order == null) {
            return false;
        }
        return cancelPendingOrder(order, AUTO_CANCEL_REASON);
    }

    @Transactional
    public int cancelExpiredPendingOrders() {
        List<OrderEntity> pendingOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getStatus, STATUS_PENDING)
                .eq(OrderEntity::getIsDeleted, 0));
        int count = 0;
        for (OrderEntity order : pendingOrders) {
            if (isExpired(order) && cancelPendingOrder(order, AUTO_CANCEL_REASON)) {
                count++;
            }
        }
        return count;
    }

    private boolean cancelIfExpired(OrderEntity order) {
        if (!isExpired(order)) {
            return false;
        }
        return cancelPendingOrder(order, AUTO_CANCEL_REASON);
    }

    private boolean cancelPendingOrder(OrderEntity order, String reason) {
        if (order == null || order.getId() == null || order.getStatus() == null || order.getStatus() != STATUS_PENDING) {
            return false;
        }
        int updated = orderMapper.update(null, new UpdateWrapper<OrderEntity>()
                .eq("id", order.getId())
                .eq("status", STATUS_PENDING)
                .set("status", STATUS_CANCELED)
                .set("remark", reason)
                .set("updated_at", LocalDateTime.now()));
        if (updated <= 0) {
            return false;
        }
        order.setStatus(STATUS_CANCELED);
        appointmentService.cancelPendingById(order.getAppointmentId(), reason);
        return true;
    }

    private boolean isExpired(OrderEntity order) {
        if (order == null || order.getStatus() == null || order.getStatus() != STATUS_PENDING) {
            return false;
        }
        LocalDateTime createdAt = order.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        return !LocalDateTime.now().isBefore(createdAt.plusMinutes(getPaymentTimeoutMinutes()));
    }

    private void fillExpireInfo(Map<String, Object> result, OrderEntity order) {
        LocalDateTime expireTime = getExpireTime(order);
        if (expireTime == null) {
            result.put("remainingSeconds", 0L);
            return;
        }
        result.put("expireTime", expireTime.toString());
        long remainingSeconds = Math.max(Duration.between(LocalDateTime.now(), expireTime).getSeconds(), 0L);
        result.put("remainingSeconds", remainingSeconds);
    }

    private Map<String, Object> buildOrderResponse(OrderEntity order) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("appointmentId", order.getAppointmentId());
        result.put("userId", order.getUserId());
        result.put("packageId", order.getPackageId());
        result.put("totalAmount", order.getTotalAmount());
        result.put("payAmount", order.getPayAmount());
        result.put("discountAmount", order.getDiscountAmount());
        result.put("status", getOrderStatusStr(order.getStatus()));
        result.put("payChannel", order.getPayChannel());
        result.put("payTime", order.getPayTime());
        result.put("remark", order.getRemark());
        result.put("createdAt", order.getCreatedAt());
        fillExpireInfo(result, order);
        return result;
    }

    private LocalDateTime getExpireTime(OrderEntity order) {
        LocalDateTime createdAt = order == null ? null : order.getCreatedAt();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
            if (order != null) {
                order.setCreatedAt(createdAt);
            }
        }
        return createdAt.plusMinutes(getPaymentTimeoutMinutes());
    }

    private int getPaymentTimeoutMinutes() {
        Integer value = systemConfigService.getIntValue("payment.order_timeout_minutes", DEFAULT_TIMEOUT_MINUTES);
        if (value == null || value <= 0) {
            return DEFAULT_TIMEOUT_MINUTES;
        }
        return value;
    }

    private void registerPaymentTimeout(OrderEntity order) {
        LocalDateTime expireTime = getExpireTime(order);
        long seconds = Math.max(Duration.between(LocalDateTime.now(), expireTime).getSeconds(), 1L);
        redisTemplate.opsForValue().set(TIMEOUT_KEY_PREFIX + order.getOrderNo(), order.getOrderNo(), Duration.ofSeconds(seconds));
    }

    private String getOrderStatusStr(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case STATUS_PENDING: return "PENDING";
            case STATUS_PAID: return "PAID";
            case STATUS_REFUNDED: return "REFUNDED";
            case STATUS_REFUNDING: return "REFUNDING";
            case STATUS_COMPLETED: return "COMPLETED";
            case STATUS_CANCELED: return "CANCELED";
            default: return "UNKNOWN";
        }
    }
}
