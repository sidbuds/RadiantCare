package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xixin.health.common.entity.DataDictionaryEntity;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.mapper.DataDictionaryMapper;
import com.xixin.health.common.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDictService {

    private final DataDictionaryMapper dataDictionaryMapper;
    private final AuditLogService auditLogService;

    public AdminDictService(DataDictionaryMapper dataDictionaryMapper, AuditLogService auditLogService) {
        this.dataDictionaryMapper = dataDictionaryMapper;
        this.auditLogService = auditLogService;
    }

    public Map<String, Object> listDictTypes(String keyword, int pageNum, int pageSize) {
        QueryWrapper<DataDictionaryEntity> queryWrapper = new QueryWrapper<DataDictionaryEntity>()
                .select("dict_type", "count(*) as dict_count")
                .eq("is_deleted", 0)
                .like(keyword != null && !keyword.trim().isEmpty(), "dict_type", keyword)
                .groupBy("dict_type")
                .orderByAsc("dict_type");
        List<Map<String, Object>> types = dataDictionaryMapper.selectMaps(queryWrapper);
        int total = types.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", types.subList(from, to));
        result.put("total", total);
        return result;
    }

    public Map<String, Object> listDictTypes(int pageNum, int pageSize) {
        return listDictTypes(null, pageNum, pageSize);
    }

    public List<DataDictionaryEntity> listDictItems(String dictType) {
        return listDictItems(dictType, null, null);
    }

    public List<DataDictionaryEntity> listDictItems(String dictType, Integer status, String keyword) {
        return dataDictionaryMapper.selectList(
                new LambdaQueryWrapper<DataDictionaryEntity>()
                        .eq(DataDictionaryEntity::getDictType, dictType)
                        .eq(status != null, DataDictionaryEntity::getStatus, status)
                        .and(keyword != null && !keyword.trim().isEmpty(), wrapper -> wrapper
                                .like(DataDictionaryEntity::getDictCode, keyword)
                                .or()
                                .like(DataDictionaryEntity::getDictName, keyword))
                        .eq(DataDictionaryEntity::getIsDeleted, 0)
                        .orderByAsc(DataDictionaryEntity::getSortNo)
                        .orderByAsc(DataDictionaryEntity::getId));
    }

    public List<DataDictionaryEntity> listEnabledItems(String dictType) {
        return listDictItems(dictType, 1, null);
    }

    public void saveDictItem(DataDictionaryEntity entity) {
        if (entity.getDictType() == null || entity.getDictType().trim().isEmpty()
                || entity.getDictCode() == null || entity.getDictCode().trim().isEmpty()
                || entity.getDictName() == null || entity.getDictName().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "字典类型、编码、名称不能为空");
        }
        DataDictionaryEntity duplicate = dataDictionaryMapper.selectOne(new LambdaQueryWrapper<DataDictionaryEntity>()
                .eq(DataDictionaryEntity::getDictType, entity.getDictType())
                .eq(DataDictionaryEntity::getDictCode, entity.getDictCode())
                .eq(DataDictionaryEntity::getIsDeleted, 0)
                .ne(entity.getId() != null, DataDictionaryEntity::getId, entity.getId())
                .last("limit 1"));
        if (duplicate != null) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "同类型字典编码已存在");
        }
        if (entity.getId() != null) {
            dataDictionaryMapper.updateById(entity);
            auditLogService.record("DICT", "UPDATE", "DATA_DICTIONARY", entity.getId());
        } else {
            entity.setIsDeleted(0);
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }
            if (entity.getSortNo() == null) {
                entity.setSortNo(0);
            }
            dataDictionaryMapper.insert(entity);
            auditLogService.record("DICT", "CREATE", "DATA_DICTIONARY", entity.getId());
        }
    }

    public void deleteDictItem(Long id) {
        DataDictionaryEntity entity = dataDictionaryMapper.selectById(id);
        if (entity != null) {
            entity.setIsDeleted(1);
            dataDictionaryMapper.updateById(entity);
            auditLogService.record("DICT", "DELETE", "DATA_DICTIONARY", id);
        }
    }
}
