package com.xixin.health.common.service;

import com.xixin.health.common.entity.OperationLogEntity;
import com.xixin.health.common.mapper.OperationLogMapper;
import com.xixin.health.common.util.AuthContext;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final OperationLogMapper operationLogMapper;

    public AuditLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public void record(String module, String action, String targetType, Object targetId) {
        OperationLogEntity log = new OperationLogEntity();
        log.setOperatorId(AuthContext.getAccountId());
        log.setOperatorName(AuthContext.get() == null ? null : AuthContext.get().getDisplayName());
        log.setOperatorType(AuthContext.getRole() == null ? null : AuthContext.getRole().name());
        log.setModule(module);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? null : String.valueOf(targetId));
        log.setBizType(targetType == null ? module : targetType);
        log.setBizId(targetId == null ? module : String.valueOf(targetId));
        log.setIsDeleted(0);
        operationLogMapper.insert(log);
    }
}
