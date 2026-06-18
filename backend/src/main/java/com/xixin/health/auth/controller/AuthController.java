package com.xixin.health.auth.controller;

import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.service.AuthService;
import com.xixin.health.common.api.ApiResult;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.security.JwtProperties;
import com.xixin.health.security.JwtTokenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService,
                          JwtTokenService jwtTokenService,
                          JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        AuthContext.LoginUser loginUser = authService.login(request);
        String token = jwtTokenService.generateToken(
                loginUser.getAccountId(),
                loginUser.getUserId(),
                loginUser.getUsername(),
                loginUser.getDisplayName(),
                loginUser.getRole()
        );
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("accessToken", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtProperties.getExpireSeconds());
        result.put("role", loginUser.getRole().name());
        result.put("accountId", loginUser.getAccountId());
        result.put("userId", loginUser.getUserId());
        result.put("displayName", loginUser.getDisplayName());
        return ApiResult.success(result);
    }

    @GetMapping("/me")
    public ApiResult<AuthContext.LoginUser> me() {
        return ApiResult.success(AuthContext.get());
    }
}
