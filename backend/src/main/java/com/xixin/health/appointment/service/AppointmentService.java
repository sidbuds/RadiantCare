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
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.operator.service.OperatorPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预约服务 - 处理预约创建/取消/查询
 */
@Service
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ExamPackageMapper examPackageMapper;
    private final ResourceCapacityMapper resourceCapacityMapper;
    private final ExamTaskMapper examTaskMapper;
    private final OperatorPackageService operatorPackageService;

    public AppointmentService(AppointmentMapper appointmentMapper,
                              ExamPackageMapper examPackageMapper,
                              ResourceCapacityMapper resourceCapacityMapper,
                              ExamTaskMapper examTaskMapper,
                              OperatorPackageService operatorPackageService) {
        this.appointmentMapper = appointmentMapper;
        this.examPackageMapper = examPackageMapper;
        this.resourceCapacityMapper = resourceCapacityMapper;
        this.examTaskMapper = examTaskMapper;
        this.operatorPackageService = operatorPackageService;
    }

    /** 查询可用预约时段 */
    public List<ResourceCapacityEntity> availableSlots(String centerCode, Long packageId, LocalDate date) {
        examPackageExists(packageId);
        assertPackageAvailableAtCenter(packageId, centerCode);
        return resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getCenterCode, centerCode)
                .eq(ResourceCapacityEntity::getAppointDate, date)
                .eq(ResourceCapacityEntity::getResourceType, "CENTER_SLOT")
                .eq(ResourceCapacityEntity::getStatus, 1)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
    }

    /** 创建预约 */
    @Transactional
    public Map<String, Object> create(CreateAppointmentRequest request) {
        examPackageExists(request.getPackageId());
        assertPackageAvailableAtCenter(request.getPackageId(), request.getCenterCode());
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
                .eq(ResourceCapacityEntity::getResourceType, "CENTER_SLOT")
                .eq(ResourceCapacityEntity::getStatus, 1)
                .eq(ResourceCapacityEntity::getIsDeleted, 0));
        if (!capacities.isEmpty()) {
            boolean hasAvailable = false;
            for (ResourceCapacityEntity capacity : capacities) {
                int total = capacity.getCapacityTotal() == null ? 0 : capacity.getCapacityTotal();
                int used = capacity.getCapacityUsed() == null ? 0 : capacity.getCapacityUsed();
                int locked = capacity.getCapacityLocked() == null ? 0 : capacity.getCapacityLocked();
                if (total > used + locked) {
                    int updated = resourceCapacityMapper.update(null, new LambdaUpdateWrapper<ResourceCapacityEntity>()
                            .eq(ResourceCapacityEntity::getId, capacity.getId())
                            .eq(ResourceCapacityEntity::getCapacityUsed, used)
                            .apply("capacity_used + COALESCE(capacity_locked, 0) < capacity_total")
                            .set(ResourceCapacityEntity::getCapacityUsed, used + 1));
                    if (updated > 0) {
                        hasAvailable = true;
                    }
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

    /** 查询预约详情 */
    public AppointmentEntity detail(String appointmentNo) {
        AppointmentEntity entity = getByNo(appointmentNo);
        if (AuthContext.getRole() == RoleType.USER && !entity.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        fillTaskNo(entity);
        return entity;
    }

    /** 查询当前用户预约列表 */
    public List<AppointmentEntity> mine() {
        List<AppointmentEntity> appointments = appointmentMapper.selectList(new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getUserId, AuthContext.getUserId())
                .eq(AppointmentEntity::getIsDeleted, 0)
                .orderByDesc(AppointmentEntity::getId));
        for (AppointmentEntity appointment : appointments) {
            fillTaskNo(appointment);
        }
        return appointments;
    }

    /** 取消预约 */
    @Transactional
    public void cancel(String appointmentNo, String reason) {
        AppointmentEntity entity = getByNo(appointmentNo);
        if (AuthContext.getRole() == RoleType.USER && !entity.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (entity.getStatus() == null || entity.getStatus() != 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "当前预约状态不可取消，已支付预约请申请退款");
        }
        appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getId, entity.getId())
                .set(AppointmentEntity::getStatus, 3)
                .set(AppointmentEntity::getCancelReason, reason)
                .set(AppointmentEntity::getUpdatedBy, AuthContext.getUserId()));

        if (entity.getTimeSlotCode() != null && !entity.getTimeSlotCode().isEmpty()) {
            List<ResourceCapacityEntity> capacities = resourceCapacityMapper.selectList(
                    new LambdaQueryWrapper<ResourceCapacityEntity>()
                            .eq(ResourceCapacityEntity::getCenterCode, entity.getCenterCode())
                            .eq(ResourceCapacityEntity::getAppointDate, entity.getAppointDate())
                            .eq(ResourceCapacityEntity::getTimeSlotCode, entity.getTimeSlotCode())
                            .eq(ResourceCapacityEntity::getStatus, 1)
                            .eq(ResourceCapacityEntity::getIsDeleted, 0));
            for (ResourceCapacityEntity cap : capacities) {
                int used = cap.getCapacityUsed() == null ? 0 : cap.getCapacityUsed();
                if (used > 0) {
                    cap.setCapacityUsed(used - 1);
                    resourceCapacityMapper.updateById(cap);
                    break;
                }
            }
        }
    }

    /**
     * 标记爽约：将已过期且未体检的预约标记为爽约(status=4)
     * 适用条件：status=1(待体检) 且 appoint_date < 今天
     */
    public int markExpiredNoShows() {
        List<AppointmentEntity> expired = appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getStatus, 1)
                        .lt(AppointmentEntity::getAppointDate, LocalDate.now())
                        .eq(AppointmentEntity::getIsDeleted, 0));
        int count = 0;
        for (AppointmentEntity apt : expired) {
            appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                    .eq(AppointmentEntity::getId, apt.getId())
                    .eq(AppointmentEntity::getStatus, 1)
                    .set(AppointmentEntity::getStatus, 4)
                    .set(AppointmentEntity::getCancelReason, "系统自动标记爽约")
                    .set(AppointmentEntity::getUpdatedAt, LocalDateTime.now()));
            count++;
        }
        return count;
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

    private void fillTaskNo(AppointmentEntity appointment) {
        if (appointment == null || appointment.getId() == null) {
            return;
        }
        ExamTaskEntity task = examTaskMapper.selectOne(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getAppointmentId, appointment.getId())
                .eq(ExamTaskEntity::getIsDeleted, 0)
                .orderByDesc(ExamTaskEntity::getId)
                .last("limit 1"));
        if (task != null) {
            appointment.setTaskNo(task.getTaskNo());
        }
    }

    private void assertPackageAvailableAtCenter(Long packageId, String centerCode) {
        if (!operatorPackageService.isPackageAvailableAtCenter(packageId, centerCode)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "该套餐不适用于所选体检中心");
        }
    }
}
