package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.operator.dto.SaveScheduleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class OperatorScheduleService {

    private final ResourceCapacityMapper resourceCapacityMapper;

    public OperatorScheduleService(ResourceCapacityMapper resourceCapacityMapper) {
        this.resourceCapacityMapper = resourceCapacityMapper;
    }

    public List<ResourceCapacityEntity> list(String centerCode, LocalDate startDate, LocalDate endDate) {
        return resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(centerCode != null && centerCode.trim().length() > 0, ResourceCapacityEntity::getCenterCode, centerCode)
                .ge(startDate != null, ResourceCapacityEntity::getAppointDate, startDate)
                .le(endDate != null, ResourceCapacityEntity::getAppointDate, endDate)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .orderByAsc(ResourceCapacityEntity::getAppointDate)
                .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
    }

    @Transactional
    public ResourceCapacityEntity create(SaveScheduleRequest request) {
        validateCapacity(request);
        ResourceCapacityEntity entity = new ResourceCapacityEntity();
        entity.setCenterCode(request.getCenterCode());
        entity.setAppointDate(request.getAppointDate());
        entity.setTimeSlotCode(request.getTimeSlotCode());
        entity.setResourceType(request.getResourceType());
        entity.setResourceCode(request.getResourceCode());
        entity.setCapacityTotal(request.getCapacityTotal());
        entity.setCapacityUsed(0);
        entity.setCapacityLocked(0);
        entity.setStatus(request.getStatus());
        entity.setVersionNo(1);
        entity.setCreatedBy(AuthContext.getUserId());
        entity.setUpdatedBy(AuthContext.getUserId());
        entity.setIsDeleted(0);
        resourceCapacityMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ResourceCapacityEntity update(Long id, SaveScheduleRequest request) {
        validateCapacity(request);
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
                .set(ResourceCapacityEntity::getResourceType, request.getResourceType())
                .set(ResourceCapacityEntity::getResourceCode, request.getResourceCode())
                .set(ResourceCapacityEntity::getCapacityTotal, request.getCapacityTotal())
                .set(ResourceCapacityEntity::getStatus, request.getStatus())
                .set(ResourceCapacityEntity::getUpdatedBy, AuthContext.getUserId()));
        return getById(id);
    }

    private void validateCapacity(SaveScheduleRequest request) {
        if (request.getCapacityTotal() == null || request.getCapacityTotal() < 0) {
            throw new BizException(ErrorCode.OPERATOR_CAPACITY_INVALID);
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
