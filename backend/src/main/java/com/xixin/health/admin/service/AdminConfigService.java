package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.entity.SystemConfigEntity;
import com.xixin.health.common.mapper.SystemConfigMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminConfigService {

    private final SystemConfigMapper systemConfigMapper;

    public AdminConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
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
        }
    }
}
