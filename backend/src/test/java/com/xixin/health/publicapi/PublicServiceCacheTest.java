package com.xixin.health.publicapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.appointment.entity.ExamPackageCenterRelEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.mapper.ExamPackageCenterRelMapper;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import com.xixin.health.publicapi.service.PublicPackageCacheService;
import com.xixin.health.publicapi.service.PublicService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicService cache tests")
class PublicServiceCacheTest {

    @Mock
    private ExamPackageMapper examPackageMapper;
    @Mock
    private ExamPackageItemMapper examPackageItemMapper;
    @Mock
    private ExamCenterMapper examCenterMapper;
    @Mock
    private ResourceCapacityMapper resourceCapacityMapper;
    @Mock
    private ExamPackageCenterRelMapper packageCenterRelMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("package list uses cache after first query")
    void listPackagesUsesCacheAfterFirstQuery() {
        TestMybatisPlusSupport.initTableInfo(ExamPackageEntity.class, ExamPackageCenterRelEntity.class, ExamPackageItemEntity.class);
        Map<String, String> store = new HashMap<String, String>();
        PublicService service = service(store);
        when(examPackageMapper.selectList(any())).thenReturn(Collections.singletonList(packageEntity()));

        List<Map<String, Object>> first = service.listPackages(null);
        List<Map<String, Object>> second = service.listPackages(null);

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        verify(examPackageMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("package detail cache is invalidated after evict")
    void packageDetailCacheIsInvalidatedAfterEvict() {
        TestMybatisPlusSupport.initTableInfo(ExamPackageEntity.class, ExamPackageCenterRelEntity.class, ExamPackageItemEntity.class);
        Map<String, String> store = new HashMap<String, String>();
        PublicPackageCacheService cacheService = cacheService(store);
        PublicService service = new PublicService(
                examPackageMapper,
                examPackageItemMapper,
                examCenterMapper,
                resourceCapacityMapper,
                packageCenterRelMapper,
                cacheService
        );
        when(examPackageMapper.selectOne(any())).thenReturn(packageEntity());
        when(examPackageItemMapper.selectList(any())).thenReturn(Collections.singletonList(packageItem()));

        Map<String, Object> first = service.getPackageDetail("P001", null);
        cacheService.evictAll();
        Map<String, Object> second = service.getPackageDetail("P001", null);

        assertEquals("P001", first.get("packageCode"));
        assertEquals("P001", second.get("packageCode"));
        verify(examPackageMapper, times(2)).selectOne(any());
    }

    private PublicService service(Map<String, String> store) {
        return new PublicService(
                examPackageMapper,
                examPackageItemMapper,
                examCenterMapper,
                resourceCapacityMapper,
                packageCenterRelMapper,
                cacheService(store)
        );
    }

    private PublicPackageCacheService cacheService(Map<String, String> store) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenAnswer(invocation -> store.get(invocation.getArgument(0)));
        when(valueOperations.set(anyString(), anyString(), any())).thenAnswer(invocation -> {
            store.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        });
        when(valueOperations.increment(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            long next = Long.parseLong(store.getOrDefault(key, "0")) + 1L;
            store.put(key, String.valueOf(next));
            return next;
        });
        return new PublicPackageCacheService(redisTemplate, new ObjectMapper(), 300);
    }

    private ExamPackageEntity packageEntity() {
        ExamPackageEntity entity = new ExamPackageEntity();
        entity.setId(1001L);
        entity.setPackageCode("P001");
        entity.setPackageName("标准套餐");
        entity.setCategory("基础");
        entity.setPrice(new BigDecimal("199.00"));
        entity.setStatus(1);
        entity.setRemark("适合常规体检");
        entity.setIsDeleted(0);
        return entity;
    }

    private ExamPackageItemEntity packageItem() {
        ExamPackageItemEntity item = new ExamPackageItemEntity();
        item.setId(2001L);
        item.setPackageId(1001L);
        item.setItemCode("BLOOD");
        item.setItemName("血常规");
        item.setUnit("项");
        item.setRefRange("正常");
        item.setSortNo(1);
        item.setIsDeleted(0);
        return item;
    }
}
