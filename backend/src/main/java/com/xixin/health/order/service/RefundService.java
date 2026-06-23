package com.xixin.health.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.order.dto.ApplyRefundRequest;
import com.xixin.health.order.dto.RefundAuditRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.RefundApplyEntity;
import com.xixin.health.order.entity.RefundAuditLogEntity;
import com.xixin.health.order.entity.RefundRecordEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.mapper.RefundApplyMapper;
import com.xixin.health.order.mapper.RefundAuditLogMapper;
import com.xixin.health.order.mapper.RefundRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RefundService {

    private final OrderMapper orderMapper;
    private final AppointmentMapper appointmentMapper;
    private final RefundApplyMapper refundApplyMapper;
    private final RefundAuditLogMapper refundAuditLogMapper;
    private final RefundRecordMapper refundRecordMapper;

    public RefundService(OrderMapper orderMapper,
                         AppointmentMapper appointmentMapper,
                         RefundApplyMapper refundApplyMapper,
                         RefundAuditLogMapper refundAuditLogMapper,
                         RefundRecordMapper refundRecordMapper) {
        this.orderMapper = orderMapper;
        this.appointmentMapper = appointmentMapper;
        this.refundApplyMapper = refundApplyMapper;
        this.refundAuditLogMapper = refundAuditLogMapper;
        this.refundRecordMapper = refundRecordMapper;
    }

    @Transactional
    public Map<String, Object> apply(String orderNo, ApplyRefundRequest request) {
        OrderEntity order = getOrderByNo(orderNo);
        if (!order.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new BizException(ErrorCode.REFUND_ORDER_NOT_ELIGIBLE);
        }
        long processingCount = refundApplyMapper.selectCount(new LambdaQueryWrapper<RefundApplyEntity>()
                .eq(RefundApplyEntity::getOrderId, order.getId())
                .eq(RefundApplyEntity::getIsDeleted, 0)
                .in(RefundApplyEntity::getApplyStatus, 0, 1, 3));
        if (processingCount > 0) {
            throw new BizException(ErrorCode.REFUND_DUPLICATE_APPLY);
        }

        RefundApplyEntity apply = new RefundApplyEntity();
        apply.setApplyNo(NoGenerator.next("RFA"));
        apply.setOrderId(order.getId());
        apply.setOrderNo(order.getOrderNo());
        apply.setRefundType("FULL");
        apply.setApplyAmount(order.getPayAmount());
        apply.setReason(request.getReason());
        apply.setApplyStatus(0);
        apply.setAuditStatus(0);
        apply.setCreatedBy(AuthContext.getUserId());
        apply.setUpdatedBy(AuthContext.getUserId());
        apply.setIsDeleted(0);
        refundApplyMapper.insert(apply);

        orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                .eq(OrderEntity::getId, order.getId())
                .set(OrderEntity::getStatus, 3));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("applyNo", apply.getApplyNo());
        result.put("orderNo", orderNo);
        result.put("applyAmount", apply.getApplyAmount());
        result.put("status", "PENDING");
        return result;
    }

    public List<RefundApplyEntity> list(Integer applyStatus, String orderNo) {
        return refundApplyMapper.selectList(new LambdaQueryWrapper<RefundApplyEntity>()
                .eq(applyStatus != null, RefundApplyEntity::getApplyStatus, applyStatus)
                .eq(orderNo != null && orderNo.trim().length() > 0, RefundApplyEntity::getOrderNo, orderNo)
                .eq(RefundApplyEntity::getIsDeleted, 0)
                .orderByDesc(RefundApplyEntity::getId));
    }

    public Map<String, Object> detail(String applyNo) {
        RefundApplyEntity apply = getApplyByNo(applyNo);
        List<RefundAuditLogEntity> auditLogs = refundAuditLogMapper.selectList(new LambdaQueryWrapper<RefundAuditLogEntity>()
                .eq(RefundAuditLogEntity::getApplyNo, apply.getApplyNo())
                .eq(RefundAuditLogEntity::getIsDeleted, 0)
                .orderByAsc(RefundAuditLogEntity::getId));
        List<RefundRecordEntity> records = refundRecordMapper.selectList(new LambdaQueryWrapper<RefundRecordEntity>()
                .eq(RefundRecordEntity::getApplyNo, apply.getApplyNo())
                .eq(RefundRecordEntity::getIsDeleted, 0)
                .orderByAsc(RefundRecordEntity::getId));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("apply", apply);
        result.put("auditLogs", auditLogs);
        result.put("refundRecords", records);
        return result;
    }

    @Transactional
    public Map<String, Object> approve(String applyNo, RefundAuditRequest request) {
        RefundApplyEntity apply = getApplyByNo(applyNo);
        if (apply.getApplyStatus() == null || apply.getApplyStatus() != 0) {
            throw new BizException(ErrorCode.REFUND_STATUS_INVALID);
        }
        OrderEntity order = orderMapper.selectById(apply.getOrderId());
        if (order == null || order.getStatus() == null || (order.getStatus() != 1 && order.getStatus() != 3)) {
            throw new BizException(ErrorCode.REFUND_ORDER_NOT_ELIGIBLE);
        }

        refundApplyMapper.update(null, new LambdaUpdateWrapper<RefundApplyEntity>()
                .eq(RefundApplyEntity::getId, apply.getId())
                .set(RefundApplyEntity::getApplyStatus, 1)
                .set(RefundApplyEntity::getAuditStatus, 1)
                .set(RefundApplyEntity::getUpdatedBy, AuthContext.getUserId()));
        insertAuditLog(apply, "APPROVE", "PASS", request.getAuditRemark());

        refundApplyMapper.update(null, new LambdaUpdateWrapper<RefundApplyEntity>()
                .eq(RefundApplyEntity::getId, apply.getId())
                .set(RefundApplyEntity::getApplyStatus, 3)
                .set(RefundApplyEntity::getUpdatedBy, AuthContext.getUserId()));

        RefundRecordEntity record = new RefundRecordEntity();
        record.setRefundNo(NoGenerator.next("RFD"));
        record.setApplyNo(apply.getApplyNo());
        record.setOrderNo(order.getOrderNo());
        record.setThirdPartyNo(NoGenerator.next("MRF"));
        record.setRefundAmount(apply.getApplyAmount());
        record.setRefundStatus(1);
        record.setRefundTime(LocalDateTime.now());
        record.setRemark("退款成功");
        record.setCreatedBy(AuthContext.getUserId());
        record.setUpdatedBy(AuthContext.getUserId());
        record.setIsDeleted(0);
        refundRecordMapper.insert(record);

        refundApplyMapper.update(null, new LambdaUpdateWrapper<RefundApplyEntity>()
                .eq(RefundApplyEntity::getId, apply.getId())
                .set(RefundApplyEntity::getApplyStatus, 4)
                .set(RefundApplyEntity::getUpdatedBy, AuthContext.getUserId()));

        orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                .eq(OrderEntity::getId, order.getId())
                .set(OrderEntity::getStatus, 2)
                .set(OrderEntity::getUpdatedBy, AuthContext.getUserId()));

        appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getId, order.getAppointmentId())
                .set(AppointmentEntity::getStatus, 3));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("applyNo", applyNo);
        result.put("orderNo", apply.getOrderNo());
        result.put("status", "DONE");
        result.put("refundNo", record.getRefundNo());
        return result;
    }

    @Transactional
    public Map<String, Object> reject(String applyNo, RefundAuditRequest request) {
        RefundApplyEntity apply = getApplyByNo(applyNo);
        if (apply.getApplyStatus() == null || apply.getApplyStatus() != 0) {
            throw new BizException(ErrorCode.REFUND_STATUS_INVALID);
        }
        refundApplyMapper.update(null, new LambdaUpdateWrapper<RefundApplyEntity>()
                .eq(RefundApplyEntity::getId, apply.getId())
                .set(RefundApplyEntity::getApplyStatus, 2)
                .set(RefundApplyEntity::getAuditStatus, 2)
                .set(RefundApplyEntity::getUpdatedBy, AuthContext.getUserId()));
        insertAuditLog(apply, "REJECT", "REJECT", request.getAuditRemark());

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("applyNo", applyNo);
        result.put("orderNo", apply.getOrderNo());
        result.put("status", "REJECTED");
        return result;
    }

    private void insertAuditLog(RefundApplyEntity apply, String action, String result, String remark) {
        RefundAuditLogEntity log = new RefundAuditLogEntity();
        log.setApplyNo(apply.getApplyNo());
        log.setAuditorId(AuthContext.getUserId());
        log.setAuditAction(action);
        log.setAuditRemark(remark);
        log.setCreatedBy(AuthContext.getUserId());
        log.setUpdatedBy(AuthContext.getUserId());
        log.setIsDeleted(0);
        refundAuditLogMapper.insert(log);
    }

    private RefundApplyEntity getApplyByNo(String applyNo) {
        RefundApplyEntity apply = refundApplyMapper.selectOne(new LambdaQueryWrapper<RefundApplyEntity>()
                .eq(RefundApplyEntity::getApplyNo, applyNo)
                .eq(RefundApplyEntity::getIsDeleted, 0));
        if (apply == null) {
            throw new BizException(ErrorCode.REFUND_APPLY_NOT_FOUND);
        }
        return apply;
    }

    private OrderEntity getOrderByNo(String orderNo) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getIsDeleted, 0));
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }
}