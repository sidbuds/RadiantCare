package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.AuditLogService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.operator.dto.SaveScheduleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class OperatorScheduleService {

    private static final String CENTER_SLOT = "CENTER_SLOT";

    private final ResourceCapacityMapper resourceCapacityMapper;
    private final AuditLogService auditLogService;

    public OperatorScheduleService(ResourceCapacityMapper resourceCapacityMapper, AuditLogService auditLogService) {
        this.resourceCapacityMapper = resourceCapacityMapper;
        this.auditLogService = auditLogService;
    }

    public List<ResourceCapacityEntity> list(String centerCode, String departmentCode, LocalDate startDate, LocalDate endDate) {
        return resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(centerCode != null && centerCode.trim().length() > 0, ResourceCapacityEntity::getCenterCode, centerCode)
                .ge(startDate != null, ResourceCapacityEntity::getAppointDate, startDate)
                .le(endDate != null, ResourceCapacityEntity::getAppointDate, endDate)
                .eq(ResourceCapacityEntity::getResourceType, CENTER_SLOT)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .orderByAsc(ResourceCapacityEntity::getAppointDate)
                .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
    }

    @Transactional
    public ResourceCapacityEntity create(SaveScheduleRequest request) {
        validateCapacity(request);
        assertUniqueCenterSlot(null, request);
        ResourceCapacityEntity entity = new ResourceCapacityEntity();
        entity.setCenterCode(request.getCenterCode());
        entity.setAppointDate(request.getAppointDate());
        entity.setTimeSlotCode(request.getTimeSlotCode());
        entity.setResourceType(CENTER_SLOT);
        entity.setResourceCode(request.getCenterCode());
        entity.setCapacityTotal(request.getCapacityTotal());
        entity.setCapacityUsed(0);
        entity.setCapacityLocked(0);
        entity.setDepartmentCode(null);
        entity.setDepartmentName(null);
        entity.setStatus(request.getStatus());
        entity.setVersionNo(1);
        entity.setCreatedBy(AuthContext.getUserId());
        entity.setUpdatedBy(AuthContext.getUserId());
        entity.setIsDeleted(0);
        resourceCapacityMapper.insert(entity);
        auditLogService.record("SCHEDULE", "CREATE", "RESOURCE_CAPACITY", entity.getId());
        return entity;
    }

    @Transactional
    public ResourceCapacityEntity update(Long id, SaveScheduleRequest request) {
        validateCapacity(request);
        assertUniqueCenterSlot(id, request);
        ResourceCapacityEntity entity = getById(id);
        int used = entity.getCapacityUsed() == null ? 0 : entity.getCapacityUsed();
        int locked = entity.getCapacityLocked() == null ? 0 : entity.getCapacityLocked();
        if (request.getCapacityTotal() < used + locked) {
            throw new BizException(ErrorCode.OPERATOR_CAPACITY_INVALID);
        }
        resourceCapacityMapper.update(null, new LambdaUpdateWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getId, id)
                .set(ResourceCapacityEntity::getCenterCode, request.getCenterCode())
                .set(ResourceCapacityEntity::getAppointDate, request.getAppointDate())
                .set(ResourceCapacityEntity::getTimeSlotCode, request.getTimeSlotCode())
                .set(ResourceCapacityEntity::getResourceType, CENTER_SLOT)
                .set(ResourceCapacityEntity::getResourceCode, request.getCenterCode())
                .set(ResourceCapacityEntity::getDepartmentCode, null)
                .set(ResourceCapacityEntity::getDepartmentName, null)
                .set(ResourceCapacityEntity::getCapacityTotal, request.getCapacityTotal())
                .set(ResourceCapacityEntity::getStatus, request.getStatus())
                .set(ResourceCapacityEntity::getUpdatedBy, AuthContext.getUserId()));
        auditLogService.record("SCHEDULE", "UPDATE", "RESOURCE_CAPACITY", id);
        return getById(id);
    }

    private void validateCapacity(SaveScheduleRequest request) {
        if (request.getCapacityTotal() == null || request.getCapacityTotal() < 0) {
            throw new BizException(ErrorCode.OPERATOR_CAPACITY_INVALID);
        }
    }

    private void assertUniqueCenterSlot(Long currentId, SaveScheduleRequest request) {
        Long count = resourceCapacityMapper.selectCount(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getCenterCode, request.getCenterCode())
                .eq(ResourceCapacityEntity::getAppointDate, request.getAppointDate())
                .eq(ResourceCapacityEntity::getTimeSlotCode, request.getTimeSlotCode())
                .eq(ResourceCapacityEntity::getResourceType, CENTER_SLOT)
                .eq(ResourceCapacityEntity::getResourceCode, request.getCenterCode())
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .ne(currentId != null, ResourceCapacityEntity::getId, currentId));
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "同一中心同一天同一时段只能配置一条容量");
        }
    }

    private ResourceCapacityEntity getById(Long id) {
        ResourceCapacityEntity entity = resourceCapacityMapper.selectOne(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getId, id)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "排班容量不存在");
        }
        return entity;
    }
}
