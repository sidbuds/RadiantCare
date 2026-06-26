package com.xixin.health.user.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.user.service.HealthProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/health-profile")
@PreAuthorize("hasRole('USER')")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    public HealthProfileController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }

    @GetMapping
    public ApiResult<?> getHealthProfile() {
        return ApiResult.success(healthProfileService.getHealthProfile());
    }

    @PutMapping
    public ApiResult<?> saveHealthProfile(@RequestBody Map<String, Object> data) {
        healthProfileService.saveHealthProfile(data);
        return ApiResult.success(null);
    }
}
