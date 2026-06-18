package com.xixin.health.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AppointmentService appointmentService;
    private final ExamPackageMapper examPackageMapper;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        AppointmentService appointmentService,
                        ExamPackageMapper examPackageMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.appointmentService = appointmentService;
        this.examPackageMapper = examPackageMapper;
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
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该预约已创建订单");
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

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", order.getTotalAmount());
        result.put("discountAmount", order.getDiscountAmount());
        result.put("payAmount", order.getPayAmount());
        result.put("status", "WAIT_PAY");
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
        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
        orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                .eq(OrderEntity::getId, order.getId())
                .set(OrderEntity::getStatus, 1)
                .set(OrderEntity::getPayChannel, "MOCK")
                .set(OrderEntity::getPayTime, LocalDateTime.now()));
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
}
