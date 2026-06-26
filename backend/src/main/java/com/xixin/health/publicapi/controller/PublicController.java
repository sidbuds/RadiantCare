package com.xixin.health.publicapi.controller;

import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.common.api.ApiResult;
import com.xixin.health.publicapi.service.PublicService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 公开接口控制器 - 无需认证
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;
    private final DoctorDepartmentRelMapper doctorDepartmentRelMapper;

    public PublicController(PublicService publicService, DoctorDepartmentRelMapper doctorDepartmentRelMapper) {
        this.publicService = publicService;
        this.doctorDepartmentRelMapper = doctorDepartmentRelMapper;
    }

    /** 查询套餐列表 */
    @GetMapping("/packages")
    public ApiResult<?> listPackages() {
        return ApiResult.success(publicService.listPackages());
    }

    /** 查询套餐详情 */
    @GetMapping("/packages/{packageCode}")
    public ApiResult<?> getPackageDetail(@PathVariable String packageCode) {
        Object detail = publicService.getPackageDetail(packageCode);
        if (detail == null) {
            return ApiResult.fail(1002, "套餐不存在");
        }
        return ApiResult.success(detail);
    }

    /** 查询体检中心列表 */
    @GetMapping("/centers")
    public ApiResult<?> listCenters() {
        return ApiResult.success(publicService.listCenters());
    }

    /** 查询体检中心详情 */
    @GetMapping("/centers/{centerCode}")
    public ApiResult<?> getCenterDetail(@PathVariable String centerCode) {
        return ApiResult.success(publicService.getCenterDetail(centerCode));
    }

    /** 查询体检中心可用时段 */
    @GetMapping("/centers/{centerCode}/slots")
    public ApiResult<?> getCenterSlots(
            @PathVariable String centerCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ApiResult.success(publicService.getCenterSlots(centerCode, date));
    }

    /** 查询体检指南 */
    @GetMapping("/content/checkup-guide")
    public ApiResult<?> getCheckupGuide() {
        return ApiResult.success(publicService.getCheckupGuide());
    }

    /** 查询常见问题 */
    @GetMapping("/content/faq")
    public ApiResult<?> getFaq() {
        return ApiResult.success(publicService.getFaq());
    }

    @GetMapping("/departments")
    public ApiResult<?> listDepartments() {
        java.util.List<DoctorDepartmentRelEntity> rels = doctorDepartmentRelMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .select(DoctorDepartmentRelEntity::getDepartmentCode, DoctorDepartmentRelEntity::getDepartmentName, DoctorDepartmentRelEntity::getCenterCode)
                        .groupBy(DoctorDepartmentRelEntity::getDepartmentCode,
                                DoctorDepartmentRelEntity::getDepartmentName,
                                DoctorDepartmentRelEntity::getCenterCode));
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (DoctorDepartmentRelEntity rel : rels) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("departmentCode", rel.getDepartmentCode());
            item.put("departmentName", rel.getDepartmentName());
            item.put("centerCode", rel.getCenterCode());
            result.add(item);
        }
        return ApiResult.success(result);
    }
}
