package com.xixin.health.admin.controller;

import com.xixin.health.admin.service.AdminRoleService;
import com.xixin.health.common.api.ApiResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    public AdminRoleController(AdminRoleService adminRoleService) {
        this.adminRoleService = adminRoleService;
    }

    @GetMapping("/accounts")
    public ApiResult<?> accounts() {
        return ApiResult.success(adminRoleService.listAccounts());
    }

    @PostMapping("/bind")
    public ApiResult<?> bind(@RequestBody Map<String, Object> body) {
        Number staffAccountId = (Number) body.get("staffAccountId");
        String roleCode = (String) body.get("roleCode");
        adminRoleService.bindRole(staffAccountId == null ? null : staffAccountId.longValue(), roleCode);
        return ApiResult.success(null);
    }

    @PostMapping("/unbind")
    public ApiResult<?> unbind(@RequestBody Map<String, Object> body) {
        Number staffAccountId = (Number) body.get("staffAccountId");
        String roleCode = (String) body.get("roleCode");
        adminRoleService.unbindRole(staffAccountId == null ? null : staffAccountId.longValue(), roleCode);
        return ApiResult.success(null);
    }

    @PostMapping("/grant-user-role")
    public ApiResult<?> grantUserRole(@RequestBody Map<String, Object> body) {
        Number userId = (Number) body.get("userId");
        String roleCode = (String) body.get("roleCode");
        return ApiResult.success(adminRoleService.grantUserRole(userId == null ? null : userId.longValue(), roleCode));
    }
}
