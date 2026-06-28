package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.admin.service.AdminDictService;
import com.xixin.health.common.entity.DataDictionaryEntity;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperatorOptionService {

    public static final String DICT_EXAM_ITEM = "EXAM_ITEM";
    public static final String DICT_TIME_SLOT = "TIME_SLOT";
    public static final String DICT_EXAM_DEPARTMENT = "EXAM_DEPARTMENT";

    private final ExamCenterMapper examCenterMapper;
    private final AdminDictService adminDictService;

    public OperatorOptionService(ExamCenterMapper examCenterMapper, AdminDictService adminDictService) {
        this.examCenterMapper = examCenterMapper;
        this.adminDictService = adminDictService;
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
        List<DataDictionaryEntity> dicts = adminDictService.listEnabledItems(DICT_EXAM_DEPARTMENT);
        String normalizedCenterCode = centerCode == null || centerCode.trim().isEmpty() ? null : centerCode.trim();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DataDictionaryEntity dict : dicts) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("centerCode", normalizedCenterCode);
            item.put("departmentCode", dict.getDictCode());
            item.put("departmentName", dict.getDictName());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> examItems() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DataDictionaryEntity dict : adminDictService.listEnabledItems(DICT_EXAM_ITEM)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("itemCode", dict.getDictCode());
            item.put("itemName", dict.getDictName());
            item.put("unit", null);
            item.put("refRange", dict.getRemark());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> timeSlots(String centerCode, String departmentCode) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DataDictionaryEntity dict : adminDictService.listEnabledItems(DICT_TIME_SLOT)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("timeSlotCode", dict.getDictCode());
            item.put("timeSlotName", dict.getDictName());
            item.put("resourceType", "CENTER_SLOT");
            item.put("resourceCode", centerCode);
            result.add(item);
        }
        return result;
    }
}
