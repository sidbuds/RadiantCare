package com.xixin.health.auth;

import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.auth.service.AuthService;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.security.JwtTokenService;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock
    private StaffAccountMapper staffAccountMapper;

    @Mock
    private StaffRoleRelMapper staffRoleRelMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUserNo("U001");
        userEntity.setName("测试用户");
        userEntity.setPasswordHash("$2a$10$encodedPassword");
        userEntity.setStatus(1);
    }

    @Test
    @DisplayName("用户登录成功")
    void login_Success() {
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(userEntity);
        when(passwordEncoder.matches("123456", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtTokenService.generateToken(anyLong(), anyLong(), anyString(), anyString(), any())).thenReturn("mockToken");

        AuthContext.LoginUser result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("密码错误登录失败")
    void login_WrongPassword() {
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(userEntity);
        when(passwordEncoder.matches("wrong", "$2a$10$encodedPassword")).thenReturn(false);

        loginRequest.setPassword("wrong");
        assertThrows(BizException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("账号不存在登录失败")
    void login_UserNotFound() {
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("账号禁用登录失败")
    void login_AccountDisabled() {
        userEntity.setStatus(0);
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(userEntity);

        assertThrows(BizException.class, () -> authService.login(loginRequest));
    }
}
