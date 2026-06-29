package com.xixin.health.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.entity.SystemConfigEntity;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.mapper.SystemConfigMapper;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    public SystemConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

    public String getValue(String configKey, String defaultValue) {
        SystemConfigEntity entity = selectByKey(configKey);
        return entity == null || entity.getConfigValue() == null ? defaultValue : entity.getConfigValue();
    }

    public Integer getIntValue(String configKey, Integer defaultValue) {
        String value = getValue(configKey, defaultValue == null ? null : String.valueOf(defaultValue));
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "系统配置 " + configKey + " 不是有效整数");
        }
    }

    public Boolean getBooleanValue(String configKey, Boolean defaultValue) {
        String value = getValue(configKey, defaultValue == null ? null : String.valueOf(defaultValue));
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        String normalized = value.trim();
        if ("true".equalsIgnoreCase(normalized) || "1".equals(normalized)) {
            return true;
        }
        if ("false".equalsIgnoreCase(normalized) || "0".equals(normalized)) {
            return false;
        }
        throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "系统配置 " + configKey + " 不是有效布尔值");
    }

    private SystemConfigEntity selectByKey(String configKey) {
        return systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, configKey)
                .eq(SystemConfigEntity::getIsDeleted, 0)
                .last("limit 1"));
    }
}
