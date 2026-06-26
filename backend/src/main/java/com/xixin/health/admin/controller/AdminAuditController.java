package com.xixin.health.admin.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.admin.service.AdminAuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    public AdminAuditController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    @GetMapping
    public ApiResult<?> listLogs(@RequestParam(required = false) String module,
                                 @RequestParam(required = false) String action,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(adminAuditService.listLogs(module, action, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<?> getDetail(@PathVariable Long id) {
        return ApiResult.success(adminAuditService.getDetail(id));
    }
}
