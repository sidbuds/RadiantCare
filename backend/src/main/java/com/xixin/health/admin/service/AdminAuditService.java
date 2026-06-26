package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.entity.OperationLogEntity;
import com.xixin.health.common.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuditService {

    private final OperationLogMapper operationLogMapper;

    public AdminAuditService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public Map<String, Object> listLogs(String module, String action, int pageNum, int pageSize) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<OperationLogEntity>()
                .eq(OperationLogEntity::getIsDeleted, 0)
                .orderByDesc(OperationLogEntity::getId);
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLogEntity::getModule, module);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.like(OperationLogEntity::getAction, action);
        }
        List<OperationLogEntity> list = operationLogMapper.selectList(wrapper);
        int total = list.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list.subList(from, to));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    public OperationLogEntity getDetail(Long id) {
        return operationLogMapper.selectById(id);
    }
}
