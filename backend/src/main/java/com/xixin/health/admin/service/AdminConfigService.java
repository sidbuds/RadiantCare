package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.entity.SystemConfigEntity;
import com.xixin.health.common.mapper.SystemConfigMapper;
import com.xixin.health.common.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final AuditLogService auditLogService;

    public AdminConfigService(SystemConfigMapper systemConfigMapper, AuditLogService auditLogService) {
        this.systemConfigMapper = systemConfigMapper;
        this.auditLogService = auditLogService;
    }

    public Map<String, Object> listConfigs(String configGroup, int pageNum, int pageSize) {
        LambdaQueryWrapper<SystemConfigEntity> wrapper = new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getIsDeleted, 0)
                .orderByAsc(SystemConfigEntity::getConfigGroup, SystemConfigEntity::getConfigKey);
        if (configGroup != null && !configGroup.isEmpty()) {
            wrapper.eq(SystemConfigEntity::getConfigGroup, configGroup);
        }
        List<SystemConfigEntity> list = systemConfigMapper.selectList(wrapper);
        int total = list.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list.subList(from, to));
        result.put("total", total);
        return result;
    }

    public void updateConfig(Long id, String configValue) {
        SystemConfigEntity entity = systemConfigMapper.selectById(id);
        if (entity != null) {
            entity.setConfigValue(configValue);
            systemConfigMapper.updateById(entity);
            auditLogService.record("CONFIG", "UPDATE", "SYSTEM_CONFIG", id);
        }
    }

    public String getValue(String configKey, String defaultValue) {
        SystemConfigEntity entity = systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, configKey)
                .eq(SystemConfigEntity::getIsDeleted, 0)
                .last("limit 1"));
        return entity == null || entity.getConfigValue() == null ? defaultValue : entity.getConfigValue();
    }

    public Integer getIntValue(String configKey, Integer defaultValue) {
        String value = getValue(configKey, defaultValue == null ? null : String.valueOf(defaultValue));
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
