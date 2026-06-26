package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.exam.mapper.ExamDepartmentRouteMapper;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperatorOptionService {

    private final ExamCenterMapper examCenterMapper;
    private final ExamDepartmentRouteMapper examDepartmentRouteMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ResourceCapacityMapper resourceCapacityMapper;

    public OperatorOptionService(ExamCenterMapper examCenterMapper,
                                 ExamDepartmentRouteMapper examDepartmentRouteMapper,
                                 ExamPackageItemMapper examPackageItemMapper,
                                 ResourceCapacityMapper resourceCapacityMapper) {
        this.examCenterMapper = examCenterMapper;
        this.examDepartmentRouteMapper = examDepartmentRouteMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.resourceCapacityMapper = resourceCapacityMapper;
    }

    public List<Map<String, Object>> centers() {
        List<ExamCenterEntity> centers = examCenterMapper.selectList(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .orderByAsc(ExamCenterEntity::getCenterCode));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExamCenterEntity center : centers) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("id", center.getId());
            item.put("centerCode", center.getCenterCode());
            item.put("centerName", center.getCenterName());
            item.put("status", center.getStatus());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> departments(String centerCode) {
        List<ExamDepartmentRouteEntity> routes = examDepartmentRouteMapper.selectList(
                new LambdaQueryWrapper<ExamDepartmentRouteEntity>()
                        .eq(centerCode != null && centerCode.trim().length() > 0,
                                ExamDepartmentRouteEntity::getCenterCode, centerCode)
                        .eq(ExamDepartmentRouteEntity::getStatus, 1)
                        .eq(ExamDepartmentRouteEntity::getIsDeleted, 0)
                        .orderByAsc(ExamDepartmentRouteEntity::getCenterCode)
                        .orderByAsc(ExamDepartmentRouteEntity::getDepartmentCode));
        Map<String, Map<String, Object>> unique = new LinkedHashMap<String, Map<String, Object>>();
        for (ExamDepartmentRouteEntity route : routes) {
            String key = route.getCenterCode() + "|" + route.getDepartmentCode();
            if (!unique.containsKey(key)) {
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("centerCode", route.getCenterCode());
                item.put("departmentCode", route.getDepartmentCode());
                item.put("departmentName", route.getDepartmentName());
                unique.put(key, item);
            }
        }
        return new ArrayList<Map<String, Object>>(unique.values());
    }

    public List<Map<String, Object>> examItems() {
        List<ExamPackageItemEntity> items = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageItemEntity::getItemCode));
        Map<String, Map<String, Object>> unique = new LinkedHashMap<String, Map<String, Object>>();
        for (ExamPackageItemEntity source : items) {
            if (source.getItemCode() == null || unique.containsKey(source.getItemCode())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("itemCode", source.getItemCode());
            item.put("itemName", source.getItemName());
            item.put("unit", source.getUnit());
            item.put("refRange", source.getRefRange());
            unique.put(source.getItemCode(), item);
        }
        return new ArrayList<Map<String, Object>>(unique.values());
    }

    public List<Map<String, Object>> timeSlots(String centerCode, String departmentCode) {
        List<ResourceCapacityEntity> capacities = resourceCapacityMapper.selectList(
                new LambdaQueryWrapper<ResourceCapacityEntity>()
                        .eq(centerCode != null && centerCode.trim().length() > 0,
                                ResourceCapacityEntity::getCenterCode, centerCode)
                        .eq(departmentCode != null && departmentCode.trim().length() > 0,
                                ResourceCapacityEntity::getDepartmentCode, departmentCode)
                        .eq(ResourceCapacityEntity::getIsDeleted, 0)
                        .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
        Map<String, Map<String, Object>> unique = new LinkedHashMap<String, Map<String, Object>>();
        for (ResourceCapacityEntity capacity : capacities) {
            String key = capacity.getTimeSlotCode();
            if (key == null || unique.containsKey(key)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("timeSlotCode", capacity.getTimeSlotCode());
            item.put("resourceType", capacity.getResourceType());
            item.put("resourceCode", capacity.getResourceCode());
            unique.put(key, item);
        }
        return new ArrayList<Map<String, Object>>(unique.values());
    }
}
