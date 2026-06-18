package com.xixin.health.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.dto.CreateAppointmentRequest;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.enums.RoleType;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ExamPackageMapper examPackageMapper;
    private final ResourceCapacityMapper resourceCapacityMapper;

    public AppointmentService(AppointmentMapper appointmentMapper,
                              ExamPackageMapper examPackageMapper,
                              ResourceCapacityMapper resourceCapacityMapper) {
        this.appointmentMapper = appointmentMapper;
        this.examPackageMapper = examPackageMapper;
        this.resourceCapacityMapper = resourceCapacityMapper;
    }

    public List<ResourceCapacityEntity> availableSlots(String centerCode, Long packageId, LocalDate date) {
        examPackageExists(packageId);
        return resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getCenterCode, centerCode)
                .eq(ResourceCapacityEntity::getAppointDate, date)
                .eq(ResourceCapacityEntity::getStatus, 1)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
    }

    @Transactional
    public Map<String, Object> create(CreateAppointmentRequest request) {
        examPackageExists(request.getPackageId());
        Long userId = AuthContext.getUserId();
        long duplicateCount = appointmentMapper.selectCount(new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getUserId, userId)
                .eq(AppointmentEntity::getPackageId, request.getPackageId())
                .eq(AppointmentEntity::getAppointDate, request.getAppointDate())
                .in(AppointmentEntity::getStatus, 0, 1, 2)
                .eq(AppointmentEntity::getIsDeleted, 0));
        if (duplicateCount > 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "同一日期不可重复预约同一套餐");
        }

        List<ResourceCapacityEntity> capacities = resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getCenterCode, request.getCenterCode())
                .eq(ResourceCapacityEntity::getAppointDate, request.getAppointDate())
                .eq(ResourceCapacityEntity::getTimeSlotCode, request.getTimeSlotCode())
                .eq(ResourceCapacityEntity::getStatus, 1)
                .eq(ResourceCapacityEntity::getIsDeleted, 0));
        if (!capacities.isEmpty()) {
            boolean hasAvailable = false;
            for (ResourceCapacityEntity capacity : capacities) {
                int total = capacity.getCapacityTotal() == null ? 0 : capacity.getCapacityTotal();
                int used = capacity.getCapacityUsed() == null ? 0 : capacity.getCapacityUsed();
                int locked = capacity.getCapacityLocked() == null ? 0 : capacity.getCapacityLocked();
                if (total > used + locked) {
                    hasAvailable = true;
                    capacity.setCapacityUsed(used + 1);
                    resourceCapacityMapper.updateById(capacity);
                    break;
                }
            }
            if (!hasAvailable) {
                throw new BizException(ErrorCode.APPOINTMENT_FULL);
            }
        }

        AppointmentEntity entity = new AppointmentEntity();
        entity.setAppointmentNo(NoGenerator.next("APT"));
        entity.setUserId(userId);
        entity.setPackageId(request.getPackageId());
        entity.setCenterCode(request.getCenterCode());
        entity.setAppointDate(request.getAppointDate());
        entity.setTimeSlotCode(request.getTimeSlotCode());
        entity.setStatus(0);
        entity.setRemark(request.getRemark());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setIsDeleted(0);
        appointmentMapper.insert(entity);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("appointmentNo", entity.getAppointmentNo());
        result.put("status", "PENDING");
        return result;
    }

    public AppointmentEntity detail(String appointmentNo) {
        AppointmentEntity entity = getByNo(appointmentNo);
        if (AuthContext.getRole() == RoleType.USER && !entity.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return entity;
    }

    public List<AppointmentEntity> mine() {
        return appointmentMapper.selectList(new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getUserId, AuthContext.getUserId())
                .eq(AppointmentEntity::getIsDeleted, 0)
                .orderByDesc(AppointmentEntity::getId));
    }

    @Transactional
    public void cancel(String appointmentNo, String reason) {
        AppointmentEntity entity = getByNo(appointmentNo);
        if (AuthContext.getRole() == RoleType.USER && !entity.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (entity.getStatus() == null || (entity.getStatus() != 0 && entity.getStatus() != 1)) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "当前预约状态不可取消");
        }
        appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getId, entity.getId())
                .set(AppointmentEntity::getStatus, 3)
                .set(AppointmentEntity::getCancelReason, reason)
                .set(AppointmentEntity::getUpdatedBy, AuthContext.getUserId()));
    }

    public AppointmentEntity getByNo(String appointmentNo) {
        AppointmentEntity entity = appointmentMapper.selectOne(new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getAppointmentNo, appointmentNo)
                .eq(AppointmentEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "预约不存在");
        }
        return entity;
    }

    private void examPackageExists(Long packageId) {
        ExamPackageEntity packageEntity = examPackageMapper.selectById(packageId);
        if (packageEntity == null || packageEntity.getStatus() == null || packageEntity.getStatus() != 1) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "套餐不存在或不可用");
        }
    }
}
