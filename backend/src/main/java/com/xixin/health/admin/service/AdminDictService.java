package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.entity.DataDictionaryEntity;
import com.xixin.health.common.mapper.DataDictionaryMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDictService {

    private final DataDictionaryMapper dataDictionaryMapper;

    public AdminDictService(DataDictionaryMapper dataDictionaryMapper) {
        this.dataDictionaryMapper = dataDictionaryMapper;
    }

    public Map<String, Object> listDictTypes(int pageNum, int pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataDictionaryEntity> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataDictionaryEntity>()
                        .select("dict_type", "count(*) as dict_count")
                        .eq("is_deleted", 0)
                        .groupBy("dict_type")
                        .orderByAsc("dict_type");
        List<Map<String, Object>> types = dataDictionaryMapper.selectMaps(queryWrapper);
        int total = types.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<>();
        result.put("list", types.subList(from, to));
        result.put("total", total);
        return result;
    }

    public List<DataDictionaryEntity> listDictItems(String dictType) {
        return dataDictionaryMapper.selectList(
                new LambdaQueryWrapper<DataDictionaryEntity>()
                        .eq(DataDictionaryEntity::getDictType, dictType)
                        .eq(DataDictionaryEntity::getIsDeleted, 0)
                        .orderByAsc(DataDictionaryEntity::getSortNo));
    }

    public void saveDictItem(DataDictionaryEntity entity) {
        if (entity.getId() != null) {
            dataDictionaryMapper.updateById(entity);
        } else {
            entity.setIsDeleted(0);
            dataDictionaryMapper.insert(entity);
        }
    }

    public void deleteDictItem(Long id) {
        DataDictionaryEntity entity = dataDictionaryMapper.selectById(id);
        if (entity != null) {
            entity.setIsDeleted(1);
            dataDictionaryMapper.updateById(entity);
        }
    }
}
