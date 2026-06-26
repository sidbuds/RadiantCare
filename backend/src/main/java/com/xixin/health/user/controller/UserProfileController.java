package com.xixin.health.user.controller;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.user.dto.ChangePasswordRequest;
import com.xixin.health.user.dto.UpdateProfileRequest;
import com.xixin.health.user.service.UserProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
@PreAuthorize("hasRole('USER')")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResult<?> getProfile() {
        return ApiResult.success(userProfileService.getProfile());
    }

    @PutMapping
    public ApiResult<?> updateProfile(@Validated @RequestBody UpdateProfileRequest request) {
        userProfileService.updateProfile(request);
        return ApiResult.success(null);
    }

    @PostMapping("/password")
    public ApiResult<?> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(request);
        return ApiResult.success(null);
    }
}
