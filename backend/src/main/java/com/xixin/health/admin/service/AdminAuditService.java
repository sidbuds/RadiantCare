package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.entity.OperationLogEntity;
import com.xixin.health.common.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuditService {

    private final OperationLogMapper operationLogMapper;

    public AdminAuditService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public Map<String, Object> listLogs(String module,
                                        String action,
                                        Long operatorId,
                                        String operatorName,
                                        String targetType,
                                        String targetId,
                                        LocalDateTime startTime,
                                        LocalDateTime endTime,
                                        int pageNum,
                                        int pageSize) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<OperationLogEntity>()
                .eq(OperationLogEntity::getIsDeleted, 0)
                .eq(module != null && !module.isEmpty(), OperationLogEntity::getModule, module)
                .like(action != null && !action.isEmpty(), OperationLogEntity::getAction, action)
                .eq(operatorId != null, OperationLogEntity::getOperatorId, operatorId)
                .like(operatorName != null && !operatorName.isEmpty(), OperationLogEntity::getOperatorName, operatorName)
                .eq(targetType != null && !targetType.isEmpty(), OperationLogEntity::getTargetType, targetType)
                .eq(targetId != null && !targetId.isEmpty(), OperationLogEntity::getTargetId, targetId)
                .ge(startTime != null, OperationLogEntity::getCreatedAt, startTime)
                .le(endTime != null, OperationLogEntity::getCreatedAt, endTime)
                .orderByDesc(OperationLogEntity::getId);
        List<OperationLogEntity> list = operationLogMapper.selectList(wrapper);
        int total = list.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", list.subList(from, to));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    public Map<String, Object> listLogs(String module, String action, int pageNum, int pageSize) {
        return listLogs(module, action, null, null, null, null, null, null, pageNum, pageSize);
    }

    public OperationLogEntity getDetail(Long id) {
        return operationLogMapper.selectById(id);
    }
}
