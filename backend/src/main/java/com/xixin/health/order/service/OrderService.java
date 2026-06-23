package com.xixin.health.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.order.dto.CreateOrderRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.OrderItemEntity;
import com.xixin.health.order.mapper.OrderItemMapper;
import com.xixin.health.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;
    private final ExamPackageMapper examPackageMapper;
    private final PaymentService paymentService;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        AppointmentService appointmentService,
                        AppointmentMapper appointmentMapper,
                        ExamPackageMapper examPackageMapper,
                        PaymentService paymentService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.appointmentService = appointmentService;
        this.appointmentMapper = appointmentMapper;
        this.examPackageMapper = examPackageMapper;
        this.paymentService = paymentService;
    }

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
        order.setStatus(0);
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
        return result;
    }

    public OrderEntity detail(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        if (!order.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    public List<OrderEntity> mine() {
        return orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, AuthContext.getUserId())
                .eq(OrderEntity::getIsDeleted, 0)
                .orderByDesc(OrderEntity::getId));
    }

    @Transactional
    public Map<String, Object> pay(String orderNo) {
        OrderEntity order = detail(orderNo);
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        Map<String, Object> payResult = paymentService.createPayment(order);
        log.info("Payment created: orderNo={}, transactionNo={}", orderNo, payResult.get("transactionNo"));

        orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getStatus, 0)
                .set(OrderEntity::getStatus, 1)
                .set(OrderEntity::getPayChannel, "MOCK_SANDBOX")
                .set(OrderEntity::getPayTime, LocalDateTime.now()));

        appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getId, order.getAppointmentId())
                .eq(AppointmentEntity::getStatus, 0)
                .set(AppointmentEntity::getStatus, 1));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("orderNo", orderNo);
        result.put("status", "PAID");
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

    private String getOrderStatusStr(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case 0: return "PENDING";
            case 1: return "PAID";
            case 2: return "REFUNDED";
            case 3: return "REFUNDING";
            default: return "UNKNOWN";
        }
    }
}
