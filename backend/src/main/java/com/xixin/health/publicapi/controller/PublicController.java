package com.xixin.health.publicapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.common.api.ApiResult;
import com.xixin.health.publicapi.service.PublicService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;
    private final DoctorDepartmentRelMapper doctorDepartmentRelMapper;

    public PublicController(PublicService publicService, DoctorDepartmentRelMapper doctorDepartmentRelMapper) {
        this.publicService = publicService;
        this.doctorDepartmentRelMapper = doctorDepartmentRelMapper;
    }

    @GetMapping("/packages")
    public ApiResult<?> listPackages(@RequestParam(required = false) String centerCode) {
        return ApiResult.success(publicService.listPackages(centerCode));
    }

    @GetMapping("/packages/{packageCode}")
    public ApiResult<?> getPackageDetail(@PathVariable String packageCode,
                                         @RequestParam(required = false) String centerCode) {
        Object detail = publicService.getPackageDetail(packageCode, centerCode);
        if (detail == null) {
            return ApiResult.fail(1002, "套餐不存在");
        }
        return ApiResult.success(detail);
    }

    @GetMapping("/centers")
    public ApiResult<?> listCenters() {
        return ApiResult.success(publicService.listCenters());
    }

    @GetMapping("/centers/{centerCode}")
    public ApiResult<?> getCenterDetail(@PathVariable String centerCode) {
        return ApiResult.success(publicService.getCenterDetail(centerCode));
    }

    @GetMapping("/centers/{centerCode}/slots")
    public ApiResult<?> getCenterSlots(@PathVariable String centerCode,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ApiResult.success(publicService.getCenterSlots(centerCode, date));
    }

    @GetMapping("/content/checkup-guide")
    public ApiResult<?> getCheckupGuide() {
        return ApiResult.success(publicService.getCheckupGuide());
    }

    @GetMapping("/content/appointment-config")
    public ApiResult<?> getAppointmentConfig() {
        return ApiResult.success(publicService.getAppointmentConfig());
    }

    @GetMapping("/content/faq")
    public ApiResult<?> getFaq() {
        return ApiResult.success(publicService.getFaq());
    }

    @GetMapping("/departments")
    public ApiResult<?> listDepartments() {
        List<DoctorDepartmentRelEntity> rels = doctorDepartmentRelMapper.selectList(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .select(DoctorDepartmentRelEntity::getDepartmentCode,
                                DoctorDepartmentRelEntity::getDepartmentName,
                                DoctorDepartmentRelEntity::getCenterCode)
                        .groupBy(DoctorDepartmentRelEntity::getDepartmentCode,
                                DoctorDepartmentRelEntity::getDepartmentName,
                                DoctorDepartmentRelEntity::getCenterCode));
        List<java.util.Map<String, Object>> result = new ArrayList<java.util.Map<String, Object>>();
        for (DoctorDepartmentRelEntity rel : rels) {
            java.util.Map<String, Object> item = new HashMap<String, Object>();
            item.put("departmentCode", rel.getDepartmentCode());
            item.put("departmentName", rel.getDepartmentName());
            item.put("centerCode", rel.getCenterCode());
            result.add(item);
        }
        return ApiResult.success(result);
    }
}
