package com.xixin.health.admin.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.admin.service.AdminDoctorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    public AdminDoctorController(AdminDoctorService adminDoctorService) {
        this.adminDoctorService = adminDoctorService;
    }

    @GetMapping
    public ApiResult<?> listDoctors(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.success(adminDoctorService.listDoctors(keyword, pageNum, pageSize));
    }

    @PutMapping("/{id}")
    public ApiResult<?> updateDoctor(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        adminDoctorService.updateDoctor(id, data);
        return ApiResult.success(null);
    }

    @PostMapping("/{doctorId}/departments")
    public ApiResult<?> bindDepartment(@PathVariable Long doctorId, @RequestBody Map<String, Object> data) {
        adminDoctorService.bindDepartment(doctorId,
                (String) data.get("departmentCode"),
                (String) data.get("departmentName"),
                (String) data.get("centerCode"),
                (Boolean) data.get("isPrimary"));
        return ApiResult.success(null);
    }

    @PostMapping("/departments/{relId}/unbind")
    public ApiResult<?> unbindDepartment(@PathVariable Long relId) {
        adminDoctorService.unbindDepartment(relId);
        return ApiResult.success(null);
    }
}
