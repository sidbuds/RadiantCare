package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.operator.dto.PackageItemRequest;
import com.xixin.health.operator.dto.SavePackageRequest;
import com.xixin.health.report.entity.ReportTemplateEntity;
import com.xixin.health.report.mapper.ReportTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperatorPackageService {

    private final ExamPackageMapper examPackageMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ReportTemplateMapper reportTemplateMapper;

    public OperatorPackageService(ExamPackageMapper examPackageMapper,
                                  ExamPackageItemMapper examPackageItemMapper,
                                  ReportTemplateMapper reportTemplateMapper) {
        this.examPackageMapper = examPackageMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.reportTemplateMapper = reportTemplateMapper;
    }

    public List<Map<String, Object>> list(String packageName, Integer status) {
        List<ExamPackageEntity> packages = examPackageMapper.selectList(new LambdaQueryWrapper<ExamPackageEntity>()
                .like(packageName != null && packageName.trim().length() > 0, ExamPackageEntity::getPackageName, packageName)
                .eq(status != null, ExamPackageEntity::getStatus, status)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .orderByDesc(ExamPackageEntity::getId));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExamPackageEntity entity : packages) {
            result.add(buildPackageSummary(entity));
        }
        return result;
    }

    public Map<String, Object> detail(Long id) {
        ExamPackageEntity entity = getById(id);
        List<ExamPackageItemEntity> items = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, id)
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageItemEntity::getSortNo)
                .orderByAsc(ExamPackageItemEntity::getId));
        Map<String, Object> result = buildPackageSummary(entity);
        result.put("items", items);
        return result;
    }

    @Transactional
    public Map<String, Object> create(SavePackageRequest request) {
        validateItems(request.getItems());
        long count = examPackageMapper.selectCount(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getPackageCode, request.getPackageCode())
                .eq(ExamPackageEntity::getIsDeleted, 0));
        if (count > 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "套餐编码已存在");
        }

        ExamPackageEntity entity = new ExamPackageEntity();
        entity.setPackageCode(request.getPackageCode());
        entity.setPackageName(request.getPackageName());
        entity.setCategory(request.getCategory());
        entity.setPrice(request.getPrice());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        entity.setCreatedBy(AuthContext.getUserId());
        entity.setUpdatedBy(AuthContext.getUserId());
        entity.setIsDeleted(0);
        examPackageMapper.insert(entity);

        saveItems(entity.getId(), request.getItems());
        bindTemplate(entity, request.getTemplateCode());
        return detail(entity.getId());
    }

    @Transactional
    public Map<String, Object> update(Long id, SavePackageRequest request) {
        validateItems(request.getItems());
        ExamPackageEntity entity = getById(id);
        examPackageMapper.update(null, new LambdaUpdateWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getId, id)
                .set(ExamPackageEntity::getPackageCode, request.getPackageCode())
                .set(ExamPackageEntity::getPackageName, request.getPackageName())
                .set(ExamPackageEntity::getCategory, request.getCategory())
                .set(ExamPackageEntity::getPrice, request.getPrice())
                .set(ExamPackageEntity::getStatus, request.getStatus())
                .set(ExamPackageEntity::getRemark, request.getRemark())
                .set(ExamPackageEntity::getUpdatedBy, AuthContext.getUserId()));
        examPackageItemMapper.delete(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, id));
        saveItems(id, request.getItems());
        entity.setId(id);
        entity.setPackageName(request.getPackageName());
        bindTemplate(entity, request.getTemplateCode());
        return detail(id);
    }

    private void saveItems(Long packageId, List<PackageItemRequest> items) {
        int sort = 1;
        for (PackageItemRequest item : items) {
            ExamPackageItemEntity entity = new ExamPackageItemEntity();
            entity.setPackageId(packageId);
            entity.setItemCode(item.getItemCode());
            entity.setItemName(item.getItemName());
            entity.setUnit(item.getUnit());
            entity.setRefRange(item.getRefRange());
            entity.setSortNo(item.getSortNo() == null ? sort++ : item.getSortNo());
            entity.setCreatedBy(AuthContext.getUserId());
            entity.setUpdatedBy(AuthContext.getUserId());
            entity.setIsDeleted(0);
            examPackageItemMapper.insert(entity);
        }
    }

    private void bindTemplate(ExamPackageEntity packageEntity, String templateCode) {
        if (templateCode == null || templateCode.trim().isEmpty()) {
            return;
        }
        ReportTemplateEntity template = reportTemplateMapper.selectOne(new LambdaQueryWrapper<ReportTemplateEntity>()
                .eq(ReportTemplateEntity::getPackageId, packageEntity.getId())
                .eq(ReportTemplateEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (template == null) {
            template = new ReportTemplateEntity();
            template.setTemplateCode(templateCode);
            template.setTemplateName(packageEntity.getPackageName() + "模板");
            template.setPackageId(packageEntity.getId());
            template.setTemplateType("STRUCTURED");
            template.setVersionNo(1);
            template.setRenderEngine("DEFAULT");
            template.setTemplateConfig("{}");
            template.setStatus(1);
            template.setCreatedBy(AuthContext.getUserId());
            template.setUpdatedBy(AuthContext.getUserId());
            template.setIsDeleted(0);
            reportTemplateMapper.insert(template);
        } else {
            reportTemplateMapper.update(null, new LambdaUpdateWrapper<ReportTemplateEntity>()
                    .eq(ReportTemplateEntity::getId, template.getId())
                    .set(ReportTemplateEntity::getTemplateCode, templateCode)
                    .set(ReportTemplateEntity::getTemplateName, packageEntity.getPackageName() + "模板")
                    .set(ReportTemplateEntity::getUpdatedBy, AuthContext.getUserId()));
        }
    }

    private Map<String, Object> buildPackageSummary(ExamPackageEntity entity) {
        ReportTemplateEntity template = reportTemplateMapper.selectOne(new LambdaQueryWrapper<ReportTemplateEntity>()
                .eq(ReportTemplateEntity::getPackageId, entity.getId())
                .eq(ReportTemplateEntity::getIsDeleted, 0)
                .last("limit 1"));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", entity.getId());
        result.put("packageCode", entity.getPackageCode());
        result.put("packageName", entity.getPackageName());
        result.put("category", entity.getCategory());
        result.put("price", entity.getPrice());
        result.put("status", entity.getStatus());
        result.put("remark", entity.getRemark());
        result.put("templateCode", template == null ? null : template.getTemplateCode());
        return result;
    }

    private void validateItems(List<PackageItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException(ErrorCode.PACKAGE_ITEM_REQUIRED);
        }
    }

    private ExamPackageEntity getById(Long id) {
        ExamPackageEntity entity = examPackageMapper.selectOne(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getId, id)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "套餐不存在");
        }
        return entity;
    }
}
