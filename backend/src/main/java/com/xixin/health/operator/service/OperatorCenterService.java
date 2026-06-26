package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class OperatorCenterService {

    private final ExamCenterMapper examCenterMapper;

    public OperatorCenterService(ExamCenterMapper examCenterMapper) {
        this.examCenterMapper = examCenterMapper;
    }

    public List<ExamCenterEntity> list(String keyword, Integer status) {
        return examCenterMapper.selectList(new LambdaQueryWrapper<ExamCenterEntity>()
                .and(keyword != null && keyword.trim().length() > 0,
                        wrapper -> wrapper.like(ExamCenterEntity::getCenterCode, keyword)
                                .or().like(ExamCenterEntity::getCenterName, keyword))
                .eq(status != null, ExamCenterEntity::getStatus, status)
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .orderByDesc(ExamCenterEntity::getId));
    }

    @Transactional
    public ExamCenterEntity create(Map<String, Object> body) {
        String centerCode = stringValue(body.get("centerCode"));
        String centerName = stringValue(body.get("centerName"));
        if (centerCode == null || centerName == null) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "中心编码和名称不能为空");
        }
        Long count = examCenterMapper.selectCount(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getCenterCode, centerCode)
                .eq(ExamCenterEntity::getIsDeleted, 0));
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "中心编码已存在");
        }
        ExamCenterEntity entity = new ExamCenterEntity();
        applyFields(entity, body);
        entity.setStatus(entity.getStatus() == null ? 1 : entity.getStatus());
        entity.setCreatedBy(AuthContext.getUserId());
        entity.setUpdatedBy(AuthContext.getUserId());
        entity.setIsDeleted(0);
        examCenterMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ExamCenterEntity update(Long id, Map<String, Object> body) {
        ExamCenterEntity entity = getById(id);
        applyFields(entity, body);
        entity.setUpdatedBy(AuthContext.getUserId());
        examCenterMapper.updateById(entity);
        return getById(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "状态只能为0或1");
        }
        getById(id);
        examCenterMapper.update(null, new LambdaUpdateWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getId, id)
                .set(ExamCenterEntity::getStatus, status)
                .set(ExamCenterEntity::getUpdatedBy, AuthContext.getUserId()));
    }

    private ExamCenterEntity getById(Long id) {
        ExamCenterEntity entity = examCenterMapper.selectOne(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getId, id)
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "体检中心不存在");
        }
        return entity;
    }

    private void applyFields(ExamCenterEntity entity, Map<String, Object> body) {
        if (body.containsKey("centerCode")) entity.setCenterCode(stringValue(body.get("centerCode")));
        if (body.containsKey("centerName")) entity.setCenterName(stringValue(body.get("centerName")));
        if (body.containsKey("address")) entity.setAddress(stringValue(body.get("address")));
        if (body.containsKey("phone")) entity.setPhone(stringValue(body.get("phone")));
        if (body.containsKey("businessHours")) entity.setBusinessHours(stringValue(body.get("businessHours")));
        if (body.containsKey("description")) entity.setDescription(stringValue(body.get("description")));
        if (body.containsKey("status")) {
            Number status = (Number) body.get("status");
            entity.setStatus(status == null ? null : status.intValue());
        }
    }

    private String stringValue(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.length() == 0 ? null : text;
    }
}
