package com.xixin.health.publicapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.appointment.entity.ExamPackageCenterRelEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.ExamPackageCenterRelMapper;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.common.service.SystemConfigService;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublicService {

    private final ExamPackageMapper examPackageMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ExamCenterMapper examCenterMapper;
    private final ResourceCapacityMapper resourceCapacityMapper;
    private final ExamPackageCenterRelMapper packageCenterRelMapper;
    private final PublicPackageCacheService packageCacheService;
    private final SystemConfigService systemConfigService;

    public PublicService(ExamPackageMapper examPackageMapper,
                         ExamPackageItemMapper examPackageItemMapper,
                         ExamCenterMapper examCenterMapper,
                         ResourceCapacityMapper resourceCapacityMapper,
                         ExamPackageCenterRelMapper packageCenterRelMapper,
                         PublicPackageCacheService packageCacheService,
                         SystemConfigService systemConfigService) {
        this.examPackageMapper = examPackageMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.examCenterMapper = examCenterMapper;
        this.resourceCapacityMapper = resourceCapacityMapper;
        this.packageCenterRelMapper = packageCenterRelMapper;
        this.packageCacheService = packageCacheService;
        this.systemConfigService = systemConfigService;
    }

    public List<Map<String, Object>> listPackages(String centerCode) {
        List<Map<String, Object>> cached = packageCacheService.getPackageList(centerCode);
        if (cached != null) {
            return cached;
        }
        LambdaQueryWrapper<ExamPackageEntity> wrapper = new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getStatus, 1)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageEntity::getId);
        List<ExamPackageEntity> packages;
        if (centerCode == null || centerCode.trim().isEmpty()) {
            packages = examPackageMapper.selectList(wrapper);
        } else {
            List<ExamPackageCenterRelEntity> rels = packageCenterRelMapper.selectList(
                    new LambdaQueryWrapper<ExamPackageCenterRelEntity>()
                            .eq(ExamPackageCenterRelEntity::getCenterCode, centerCode)
                            .eq(ExamPackageCenterRelEntity::getStatus, 1)
                            .eq(ExamPackageCenterRelEntity::getIsDeleted, 0));
            if (rels.isEmpty()) {
                return new ArrayList<Map<String, Object>>();
            }
            List<Long> packageIds = new ArrayList<Long>();
            for (ExamPackageCenterRelEntity rel : rels) {
                packageIds.add(rel.getPackageId());
            }
            packages = examPackageMapper.selectList(wrapper.in(ExamPackageEntity::getId, packageIds));
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExamPackageEntity pkg : packages) {
            result.add(toPackageMap(pkg, false));
        }
        packageCacheService.cachePackageList(centerCode, result);
        return result;
    }

    public List<Map<String, Object>> listPackages() {
        return listPackages(null);
    }

    public Map<String, Object> getPackageDetail(String packageCode, String centerCode) {
        Map<String, Object> cached = packageCacheService.getPackageDetail(packageCode, centerCode);
        if (cached != null) {
            return cached;
        }
        ExamPackageEntity pkg = examPackageMapper.selectOne(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getPackageCode, packageCode)
                .eq(ExamPackageEntity::getStatus, 1)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (pkg == null) {
            return null;
        }
        if (centerCode != null && !centerCode.trim().isEmpty()) {
            Long relCount = packageCenterRelMapper.selectCount(new LambdaQueryWrapper<ExamPackageCenterRelEntity>()
                    .eq(ExamPackageCenterRelEntity::getPackageId, pkg.getId())
                    .eq(ExamPackageCenterRelEntity::getCenterCode, centerCode)
                    .eq(ExamPackageCenterRelEntity::getStatus, 1)
                    .eq(ExamPackageCenterRelEntity::getIsDeleted, 0));
            if (relCount == null || relCount == 0) {
                return null;
            }
        }
        List<ExamPackageItemEntity> items = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, pkg.getId())
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageItemEntity::getSortNo));

        Map<String, Object> result = toPackageMap(pkg, true);
        result.put("items", items);
        packageCacheService.cachePackageDetail(packageCode, centerCode, result);
        return result;
    }

    public Map<String, Object> getPackageDetail(String packageCode) {
        return getPackageDetail(packageCode, null);
    }

    public List<ExamCenterEntity> listCenters() {
        return examCenterMapper.selectList(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getStatus, 1)
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .orderByAsc(ExamCenterEntity::getId));
    }

    public ExamCenterEntity getCenterDetail(String centerCode) {
        return examCenterMapper.selectOne(new LambdaQueryWrapper<ExamCenterEntity>()
                .eq(ExamCenterEntity::getCenterCode, centerCode)
                .eq(ExamCenterEntity::getStatus, 1)
                .eq(ExamCenterEntity::getIsDeleted, 0)
                .last("limit 1"));
    }

    public List<ResourceCapacityEntity> getCenterSlots(String centerCode, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return resourceCapacityMapper.selectList(new LambdaQueryWrapper<ResourceCapacityEntity>()
                .eq(ResourceCapacityEntity::getCenterCode, centerCode)
                .eq(ResourceCapacityEntity::getAppointDate, date)
                .eq(ResourceCapacityEntity::getResourceType, "CENTER_SLOT")
                .eq(ResourceCapacityEntity::getStatus, 1)
                .eq(ResourceCapacityEntity::getIsDeleted, 0)
                .orderByAsc(ResourceCapacityEntity::getTimeSlotCode));
    }

    public Map<String, Object> getAppointmentConfig() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("advanceDays", systemConfigService.getIntValue("appointment.advance_days", 7));
        result.put("allowToday", systemConfigService.getBooleanValue("appointment.allow_today", Boolean.FALSE));
        result.put("defaultCapacity", systemConfigService.getIntValue("schedule.default_capacity", 20));
        return result;
    }

    public Map<String, Object> getCheckupGuide() {
        Map<String, Object> guide = new LinkedHashMap<String, Object>();
        List<Map<String, String>> steps = new ArrayList<Map<String, String>>();
        steps.add(step("预约体检", "选择套餐、体检中心和日期时段，完成预约。"));
        steps.add(step("到院签到", "携带身份证到体检中心前台签到，领取导检单。"));
        steps.add(step("按路线体检", "按导引路线依次完成各科室检查项目。"));
        steps.add(step("等待报告", "体检完成后，医生录入结果并生成报告。"));
        steps.add(step("查看报告", "报告审核发布后，可在线查看详细报告和健康建议。"));
        guide.put("steps", steps);
        guide.put("tips", "体检前一天请保持清淡饮食，避免剧烈运动。体检当天请按要求空腹到院。");
        return guide;
    }

    public List<Map<String, String>> getFaq() {
        List<Map<String, String>> faqList = new ArrayList<Map<String, String>>();
        faqList.add(step("体检前需要注意什么？", "体检前一天晚间清淡饮食，避免剧烈运动和饮酒。"));
        faqList.add(step("可以取消预约吗？", "可以在允许取消时间内取消预约，已支付订单需按退款流程处理。"));
        faqList.add(step("报告多久可以出来？", "一般体检后3-5个工作日可查看电子报告。"));
        return faqList;
    }

    private Map<String, String> step(String title, String description) {
        Map<String, String> item = new LinkedHashMap<String, String>();
        item.put("title", title);
        item.put("description", description);
        item.put("question", title);
        item.put("answer", description);
        return item;
    }

    private Map<String, Object> toPackageMap(ExamPackageEntity pkg, boolean includeStatus) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", pkg.getId());
        result.put("packageCode", pkg.getPackageCode());
        result.put("packageName", pkg.getPackageName());
        result.put("category", pkg.getCategory());
        result.put("price", pkg.getPrice());
        result.put("remark", pkg.getRemark());
        if (includeStatus) {
            result.put("status", pkg.getStatus());
        }
        List<ExamPackageCenterRelEntity> rels = packageCenterRelMapper.selectList(
                new LambdaQueryWrapper<ExamPackageCenterRelEntity>()
                        .eq(ExamPackageCenterRelEntity::getPackageId, pkg.getId())
                        .eq(ExamPackageCenterRelEntity::getStatus, 1)
                        .eq(ExamPackageCenterRelEntity::getIsDeleted, 0)
                        .orderByAsc(ExamPackageCenterRelEntity::getCenterCode));
        List<String> centerCodes = new ArrayList<String>();
        for (ExamPackageCenterRelEntity rel : rels) {
            centerCodes.add(rel.getCenterCode());
        }
        result.put("centerCodes", centerCodes);
        return result;
    }
}
