package com.xixin.health.admin.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.admin.service.AdminAuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

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
                                 @RequestParam(required = false) Long operatorId,
                                 @RequestParam(required = false) String operatorName,
                                 @RequestParam(required = false) String targetType,
                                 @RequestParam(required = false) String targetId,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(adminAuditService.listLogs(module, action, operatorId, operatorName,
                targetType, targetId, startTime, endTime, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<?> getDetail(@PathVariable Long id) {
        return ApiResult.success(adminAuditService.getDetail(id));
    }
}
