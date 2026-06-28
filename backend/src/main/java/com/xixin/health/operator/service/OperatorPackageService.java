package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.ExamPackageCenterRelEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.mapper.ExamPackageCenterRelMapper;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.AuditLogService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.mapper.ExamDepartmentRouteMapper;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.operator.dto.PackageItemRequest;
import com.xixin.health.operator.dto.SavePackageRequest;
import com.xixin.health.operator.dto.SavePackageRouteRequest;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import com.xixin.health.publicapi.service.PublicPackageCacheService;
import com.xixin.health.report.entity.ReportTemplateEntity;
import com.xixin.health.report.mapper.ReportTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OperatorPackageService {

    private final ExamPackageMapper examPackageMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ReportTemplateMapper reportTemplateMapper;
    private final OrderMapper orderMapper;
    private final ExamTaskMapper examTaskMapper;
    private final ExamPackageCenterRelMapper packageCenterRelMapper;
    private final ExamCenterMapper examCenterMapper;
    private final ExamDepartmentRouteMapper routeMapper;
    private final AuditLogService auditLogService;
    private final PublicPackageCacheService packageCacheService;

    public OperatorPackageService(ExamPackageMapper examPackageMapper,
                                  ExamPackageItemMapper examPackageItemMapper,
                                  ReportTemplateMapper reportTemplateMapper,
                                  OrderMapper orderMapper,
                                  ExamTaskMapper examTaskMapper,
                                  ExamPackageCenterRelMapper packageCenterRelMapper,
                                  ExamCenterMapper examCenterMapper,
                                  ExamDepartmentRouteMapper routeMapper,
                                  AuditLogService auditLogService,
                                  PublicPackageCacheService packageCacheService) {
        this.examPackageMapper = examPackageMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.reportTemplateMapper = reportTemplateMapper;
        this.orderMapper = orderMapper;
        this.examTaskMapper = examTaskMapper;
        this.packageCenterRelMapper = packageCenterRelMapper;
        this.examCenterMapper = examCenterMapper;
        this.routeMapper = routeMapper;
        this.auditLogService = auditLogService;
        this.packageCacheService = packageCacheService;
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
        result.put("centers", listCenters(id));
        result.put("centerCodes", listCenterCodes(id));
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
        auditLogService.record("PACKAGE", status == 1 ? "ENABLE" : "DISABLE", "EXAM_PACKAGE", id);
        packageCacheService.evictAll();
    }

    @Transactional
    public Map<String, Object> create(SavePackageRequest request) {
        validateItems(request.getItems());
        validateCenters(request.getCenterCodes());
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
        syncCenters(entity.getId(), request.getCenterCodes());
        bindTemplate(entity);
        auditLogService.record("PACKAGE", "CREATE", "EXAM_PACKAGE", entity.getId());
        packageCacheService.evictAll();
        return detail(entity.getId());
    }

    @Transactional
    public Map<String, Object> update(Long id, SavePackageRequest request) {
        validateItems(request.getItems());
        validateCenters(request.getCenterCodes());
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
        entity.setPackageCode(request.getPackageCode());
        entity.setPackageName(request.getPackageName());
        syncCenters(id, request.getCenterCodes());
        bindTemplate(entity);
        auditLogService.record("PACKAGE", "UPDATE", "EXAM_PACKAGE", id);
        packageCacheService.evictAll();
        return detail(id);
    }

    public List<ExamDepartmentRouteEntity> routes(Long packageId, String centerCode) {
        return routeMapper.selectList(new LambdaQueryWrapper<ExamDepartmentRouteEntity>()
                .eq(ExamDepartmentRouteEntity::getPackageId, packageId)
                .eq(ExamDepartmentRouteEntity::getCenterCode, centerCode)
                .eq(ExamDepartmentRouteEntity::getIsDeleted, 0)
                .orderByAsc(ExamDepartmentRouteEntity::getRouteSort)
                .orderByAsc(ExamDepartmentRouteEntity::getId));
    }

    @Transactional
    public List<ExamDepartmentRouteEntity> saveRoutes(Long packageId, SavePackageRouteRequest request) {
        getById(packageId);
        if (!isPackageAvailableAtCenter(packageId, request.getCenterCode())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "套餐未适用于该体检中心");
        }
        List<ExamPackageItemEntity> packageItems = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, packageId)
                .eq(ExamPackageItemEntity::getIsDeleted, 0));
        Set<String> requiredItems = new LinkedHashSet<String>();
        for (ExamPackageItemEntity item : packageItems) {
            requiredItems.add(item.getItemCode());
        }
        Set<String> routeItems = new LinkedHashSet<String>();
        for (SavePackageRouteRequest.RouteItem route : request.getRoutes()) {
            routeItems.add(route.getItemCode());
        }
        if (!routeItems.containsAll(requiredItems)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "该中心导引路线未覆盖套餐全部检查项");
        }

        List<ExamDepartmentRouteEntity> existingRoutes = routeMapper.selectList(new LambdaQueryWrapper<ExamDepartmentRouteEntity>()
                .eq(ExamDepartmentRouteEntity::getPackageId, packageId)
                .eq(ExamDepartmentRouteEntity::getCenterCode, request.getCenterCode())
                .eq(ExamDepartmentRouteEntity::getIsDeleted, 0));
        Map<String, ExamDepartmentRouteEntity> existingByItem = new HashMap<String, ExamDepartmentRouteEntity>();
        for (ExamDepartmentRouteEntity existing : existingRoutes) {
            existingByItem.put(existing.getItemCode(), existing);
        }
        for (ExamDepartmentRouteEntity existing : existingRoutes) {
            if (!routeItems.contains(existing.getItemCode())) {
                routeMapper.deleteById(existing.getId());
            }
        }

        int sort = 1;
        for (SavePackageRouteRequest.RouteItem item : request.getRoutes()) {
            ExamDepartmentRouteEntity route = existingByItem.get(item.getItemCode());
            boolean inserting = route == null;
            if (inserting) {
                route = new ExamDepartmentRouteEntity();
                route.setRouteCode(NoGenerator.next("RT"));
                route.setCenterCode(request.getCenterCode());
                route.setPackageId(packageId);
                route.setItemCode(item.getItemCode());
                route.setCreatedBy(AuthContext.getUserId());
            }
            route.setItemCode(item.getItemCode());
            route.setDepartmentCode(item.getDepartmentCode());
            route.setDepartmentName(item.getDepartmentName());
            route.setRoomNo(item.getRoomNo());
            route.setFloorNo(item.getFloorNo());
            route.setBuildingNo(item.getBuildingNo());
            route.setRouteSort(item.getRouteSort() == null ? sort : item.getRouteSort());
            route.setGuideText(item.getGuideText());
            route.setNeedEmptyStomach(item.getNeedEmptyStomach() == null ? 0 : item.getNeedEmptyStomach());
            route.setStatus(item.getStatus() == null ? 1 : item.getStatus());
            route.setUpdatedBy(AuthContext.getUserId());
            route.setIsDeleted(0);
            if (inserting) {
                routeMapper.insert(route);
            } else {
                routeMapper.updateById(route);
            }
            sort++;
        }
        auditLogService.record("ROUTE", "UPDATE", "EXAM_PACKAGE", packageId + ":" + request.getCenterCode());
        packageCacheService.evictAll();
        return routes(packageId, request.getCenterCode());
    }

    public boolean isPackageAvailableAtCenter(Long packageId, String centerCode) {
        Long count = packageCenterRelMapper.selectCount(new LambdaQueryWrapper<ExamPackageCenterRelEntity>()
                .eq(ExamPackageCenterRelEntity::getPackageId, packageId)
                .eq(ExamPackageCenterRelEntity::getCenterCode, centerCode)
                .eq(ExamPackageCenterRelEntity::getStatus, 1)
                .eq(ExamPackageCenterRelEntity::getIsDeleted, 0));
        return count != null && count > 0;
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

    private void bindTemplate(ExamPackageEntity packageEntity) {
        String templateCode = "TPL-" + packageEntity.getPackageCode();
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

    private void syncCenters(Long packageId, List<String> centerCodes) {
        packageCenterRelMapper.update(null, new LambdaUpdateWrapper<ExamPackageCenterRelEntity>()
                .eq(ExamPackageCenterRelEntity::getPackageId, packageId)
                .eq(ExamPackageCenterRelEntity::getIsDeleted, 0)
                .set(ExamPackageCenterRelEntity::getIsDeleted, 1)
                .set(ExamPackageCenterRelEntity::getStatus, 0));
        Set<String> uniqueCodes = new LinkedHashSet<String>(centerCodes);
        for (String centerCode : uniqueCodes) {
            ExamCenterEntity center = examCenterMapper.selectOne(new LambdaQueryWrapper<ExamCenterEntity>()
                    .eq(ExamCenterEntity::getCenterCode, centerCode)
                    .eq(ExamCenterEntity::getIsDeleted, 0)
                    .last("limit 1"));
            if (center == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "体检中心不存在：" + centerCode);
            }
            ExamPackageCenterRelEntity rel = new ExamPackageCenterRelEntity();
            rel.setPackageId(packageId);
            rel.setCenterCode(center.getCenterCode());
            rel.setCenterName(center.getCenterName());
            rel.setStatus(1);
            rel.setCreatedBy(AuthContext.getUserId());
            rel.setUpdatedBy(AuthContext.getUserId());
            rel.setIsDeleted(0);
            packageCenterRelMapper.insert(rel);
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
        result.put("centerCodes", listCenterCodes(entity.getId()));
        return result;
    }

    private List<Map<String, Object>> listCenters(Long packageId) {
        List<ExamPackageCenterRelEntity> rels = packageCenterRelMapper.selectList(new LambdaQueryWrapper<ExamPackageCenterRelEntity>()
                .eq(ExamPackageCenterRelEntity::getPackageId, packageId)
                .eq(ExamPackageCenterRelEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageCenterRelEntity::getCenterCode));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExamPackageCenterRelEntity rel : rels) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("centerCode", rel.getCenterCode());
            item.put("centerName", rel.getCenterName());
            item.put("status", rel.getStatus());
            result.add(item);
        }
        return result;
    }

    private List<String> listCenterCodes(Long packageId) {
        List<ExamPackageCenterRelEntity> rels = packageCenterRelMapper.selectList(new LambdaQueryWrapper<ExamPackageCenterRelEntity>()
                .eq(ExamPackageCenterRelEntity::getPackageId, packageId)
                .eq(ExamPackageCenterRelEntity::getStatus, 1)
                .eq(ExamPackageCenterRelEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageCenterRelEntity::getCenterCode));
        List<String> result = new ArrayList<String>();
        for (ExamPackageCenterRelEntity rel : rels) {
            result.add(rel.getCenterCode());
        }
        return result;
    }

    private void validateItems(List<PackageItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException(ErrorCode.PACKAGE_ITEM_REQUIRED);
        }
    }

    private void validateCenters(List<String> centerCodes) {
        if (centerCodes == null || centerCodes.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "套餐至少选择一个适用体检中心");
        }
    }

    private void assertPackageEditable(ExamPackageEntity entity, SavePackageRequest request) {
        boolean referenced = isPackageReferenced(entity.getId());
        if (!referenced) {
            return;
        }
        if (request.getPackageCode() != null && !request.getPackageCode().equals(entity.getPackageCode())) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "套餐已被引用，不能修改套餐编码");
        }
        List<ExamPackageItemEntity> existingItems = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, entity.getId())
                .eq(ExamPackageItemEntity::getIsDeleted, 0));
        if (!sameItemCodes(existingItems, request.getItems())) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "套餐已被引用，不能增删检查项目");
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
        Set<String> existingCodes = new LinkedHashSet<String>();
        for (ExamPackageItemEntity item : existingItems) {
            existingCodes.add(item.getItemCode());
        }
        Set<String> requestCodes = new LinkedHashSet<String>();
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
