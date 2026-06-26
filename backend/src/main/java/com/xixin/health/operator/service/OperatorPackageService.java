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
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.operator.dto.PackageItemRequest;
import com.xixin.health.operator.dto.SavePackageRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.report.entity.ReportTemplateEntity;
import com.xixin.health.report.mapper.ReportTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营端套餐服务 - 套餐管理
 */
@Service
public class OperatorPackageService {

    private final ExamPackageMapper examPackageMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ReportTemplateMapper reportTemplateMapper;
    private final OrderMapper orderMapper;
    private final ExamTaskMapper examTaskMapper;

    public OperatorPackageService(ExamPackageMapper examPackageMapper,
                                  ExamPackageItemMapper examPackageItemMapper,
                                  ReportTemplateMapper reportTemplateMapper,
                                  OrderMapper orderMapper,
                                  ExamTaskMapper examTaskMapper) {
        this.examPackageMapper = examPackageMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.reportTemplateMapper = reportTemplateMapper;
        this.orderMapper = orderMapper;
        this.examTaskMapper = examTaskMapper;
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
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "状态只能为0或1");
        }
        getById(id);
        examPackageMapper.update(null, new LambdaUpdateWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getId, id)
                .set(ExamPackageEntity::getStatus, status)
                .set(ExamPackageEntity::getUpdatedBy, AuthContext.getUserId()));
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
        assertPackageEditable(entity, request);
        examPackageMapper.update(null, new LambdaUpdateWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getId, id)
                .set(ExamPackageEntity::getPackageCode, request.getPackageCode())
                .set(ExamPackageEntity::getPackageName, request.getPackageName())
                .set(ExamPackageEntity::getCategory, request.getCategory())
                .set(ExamPackageEntity::getPrice, request.getPrice())
                .set(ExamPackageEntity::getStatus, request.getStatus())
                .set(ExamPackageEntity::getRemark, request.getRemark())
                .set(ExamPackageEntity::getUpdatedBy, AuthContext.getUserId()));
        if (isPackageReferenced(id)) {
            updateReferencedItems(id, request.getItems());
        } else {
            examPackageItemMapper.delete(new LambdaQueryWrapper<ExamPackageItemEntity>()
                    .eq(ExamPackageItemEntity::getPackageId, id));
            saveItems(id, request.getItems());
        }
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

    private void assertPackageEditable(ExamPackageEntity entity, SavePackageRequest request) {
        boolean referenced = isPackageReferenced(entity.getId());
        if (!referenced) {
            return;
        }
        if (request.getPackageCode() != null && !request.getPackageCode().equals(entity.getPackageCode())) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "套餐已被订单或体检任务引用，不能修改套餐编码");
        }
        List<ExamPackageItemEntity> existingItems = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, entity.getId())
                .eq(ExamPackageItemEntity::getIsDeleted, 0));
        if (!sameItemCodes(existingItems, request.getItems())) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "套餐已被订单或体检任务引用，不能增删检查项目，请创建新套餐版本");
        }
    }

    private boolean isPackageReferenced(Long packageId) {
        Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getPackageId, packageId)
                .eq(OrderEntity::getIsDeleted, 0));
        if (orderCount != null && orderCount > 0) {
            return true;
        }
        Long taskCount = examTaskMapper.selectCount(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getPackageId, packageId)
                .eq(ExamTaskEntity::getIsDeleted, 0));
        return taskCount != null && taskCount > 0;
    }

    private boolean sameItemCodes(List<ExamPackageItemEntity> existingItems, List<PackageItemRequest> requestItems) {
        if (existingItems.size() != requestItems.size()) {
            return false;
        }
        java.util.Set<String> existingCodes = new java.util.HashSet<String>();
        for (ExamPackageItemEntity item : existingItems) {
            existingCodes.add(item.getItemCode());
        }
        java.util.Set<String> requestCodes = new java.util.HashSet<String>();
        for (PackageItemRequest item : requestItems) {
            requestCodes.add(item.getItemCode());
        }
        return existingCodes.equals(requestCodes);
    }

    private void updateReferencedItems(Long packageId, List<PackageItemRequest> requestItems) {
        Map<String, PackageItemRequest> requestByCode = new HashMap<String, PackageItemRequest>();
        for (PackageItemRequest item : requestItems) {
            requestByCode.put(item.getItemCode(), item);
        }
        List<ExamPackageItemEntity> existingItems = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, packageId)
                .eq(ExamPackageItemEntity::getIsDeleted, 0));
        for (ExamPackageItemEntity existing : existingItems) {
            PackageItemRequest request = requestByCode.get(existing.getItemCode());
            if (request == null) {
                continue;
            }
            examPackageItemMapper.update(null, new LambdaUpdateWrapper<ExamPackageItemEntity>()
                    .eq(ExamPackageItemEntity::getId, existing.getId())
                    .set(ExamPackageItemEntity::getItemName, request.getItemName())
                    .set(ExamPackageItemEntity::getUnit, request.getUnit())
                    .set(ExamPackageItemEntity::getRefRange, request.getRefRange())
                    .set(ExamPackageItemEntity::getSortNo, request.getSortNo())
                    .set(ExamPackageItemEntity::getUpdatedBy, AuthContext.getUserId()));
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
