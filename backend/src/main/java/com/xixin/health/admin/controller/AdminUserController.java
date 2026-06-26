package com.xixin.health.admin.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.admin.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResult<?> listUsers(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.success(adminUserService.listUsers(keyword, pageNum, pageSize));
    }

    @PostMapping("/{userId}/status")
    public ApiResult<?> updateStatus(@PathVariable Long userId, @RequestParam Integer status) {
        adminUserService.updateUserStatus(userId, status);
        return ApiResult.success(null);
    }
}
