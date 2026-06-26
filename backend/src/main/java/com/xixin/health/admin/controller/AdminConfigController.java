package com.xixin.health.admin.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.admin.service.AdminConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/configs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    public AdminConfigController(AdminConfigService adminConfigService) {
        this.adminConfigService = adminConfigService;
    }

    @GetMapping
    public ApiResult<?> listConfigs(@RequestParam(required = false) String configGroup,
                                    @RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(adminConfigService.listConfigs(configGroup, pageNum, pageSize));
    }

    @PutMapping("/{id}")
    public ApiResult<?> updateConfig(@PathVariable Long id, @RequestParam String configValue) {
        adminConfigService.updateConfig(id, configValue);
        return ApiResult.success(null);
    }
}
