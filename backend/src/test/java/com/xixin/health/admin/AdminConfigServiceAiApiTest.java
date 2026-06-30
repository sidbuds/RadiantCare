package com.xixin.health.admin;

import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.admin.service.AdminConfigService;
import com.xixin.health.common.entity.SystemConfigEntity;
import com.xixin.health.common.mapper.SystemConfigMapper;
import com.xixin.health.common.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminConfigService AI API config tests")
class AdminConfigServiceAiApiTest {

    @Mock
    private SystemConfigMapper systemConfigMapper;
    @Mock
    private AuditLogService auditLogService;

    @Test
    @DisplayName("AI API key is masked in config list")
    void aiApiKeyIsMaskedInConfigList() {
        TestMybatisPlusSupport.initTableInfo(SystemConfigEntity.class);
        AdminConfigService service = new AdminConfigService(systemConfigMapper, auditLogService);
        SystemConfigEntity key = config(1L, "ai.api.key", "sk-secret", "AI_API");
        when(systemConfigMapper.selectList(any())).thenReturn(java.util.Collections.singletonList(key));

        Map<String, Object> result = service.listConfigs("AI_API", 1, 20);
        List<?> list = (List<?>) result.get("list");
        SystemConfigEntity row = (SystemConfigEntity) list.get(0);

        assertEquals("********", row.getConfigValue());
    }

    @Test
    @DisplayName("empty AI API key update keeps existing value")
    void emptyAiApiKeyUpdateKeepsExistingValue() {
        TestMybatisPlusSupport.initTableInfo(SystemConfigEntity.class);
        AdminConfigService service = new AdminConfigService(systemConfigMapper, auditLogService);
        SystemConfigEntity key = config(1L, "ai.api.key", "sk-secret", "AI_API");
        when(systemConfigMapper.selectById(1L)).thenReturn(key);

        service.updateConfig(1L, "");

        verify(systemConfigMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("non-empty AI API key update overwrites value")
    void nonEmptyAiApiKeyUpdateOverwritesValue() {
        TestMybatisPlusSupport.initTableInfo(SystemConfigEntity.class);
        AdminConfigService service = new AdminConfigService(systemConfigMapper, auditLogService);
        SystemConfigEntity key = config(1L, "ai.api.key", "sk-secret", "AI_API");
        when(systemConfigMapper.selectById(1L)).thenReturn(key);

        service.updateConfig(1L, "sk-new");

        ArgumentCaptor<SystemConfigEntity> captor = ArgumentCaptor.forClass(SystemConfigEntity.class);
        verify(systemConfigMapper).updateById(captor.capture());
        assertEquals("sk-new", captor.getValue().getConfigValue());
    }

    private SystemConfigEntity config(Long id, String key, String value, String group) {
        SystemConfigEntity entity = new SystemConfigEntity();
        entity.setId(id);
        entity.setConfigKey(key);
        entity.setConfigValue(value);
        entity.setConfigGroup(group);
        entity.setIsDeleted(0);
        return entity;
    }
}
