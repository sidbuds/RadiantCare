package com.xixin.health.auth;

import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.auth.service.AuthService;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.enums.RoleType;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService tests")
class AuthServiceTest {

    @Mock
    private StaffAccountMapper staffAccountMapper;

    @Mock
    private StaffRoleRelMapper staffRoleRelMapper;

    @Mock
    private UserMapper userMapper;

    private AuthService authService;
    private LoginRequest loginRequest;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        TestMybatisPlusSupport.initTableInfo(UserEntity.class, StaffAccountEntity.class, StaffRoleRelEntity.class);
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUserNo("testuser");
        userEntity.setName("Test User");
        userEntity.setPasswordHash(new BCryptPasswordEncoder().encode("123456"));
        userEntity.setStatus(1);
        userEntity.setIsDeleted(0);

        authService = new AuthService(userMapper, staffAccountMapper, staffRoleRelMapper);
    }

    @Test
    @DisplayName("user login succeeds")
    void loginUserSuccess() {
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(userEntity);

        AuthContext.LoginUser result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(RoleType.USER, result.getRole());
    }

    @Test
    @DisplayName("wrong user password returns login failed")
    void loginWrongPassword() {
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(userEntity);

        loginRequest.setPassword("wrong");
        BizException exception = assertThrows(BizException.class, () -> authService.login(loginRequest));

        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("unknown account returns login failed")
    void loginUserNotFound() {
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> authService.login(loginRequest));

        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("disabled user returns banned message")
    void disabledUserReturnsBannedMessage() {
        userEntity.setStatus(0);
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(userEntity);

        BizException exception = assertThrows(BizException.class, () -> authService.login(loginRequest));

        assertEquals("账号已被封禁", exception.getMessage());
    }

    @Test
    @DisplayName("staff wrong password returns login failed instead of banned")
    void loginStaffWrongPasswordReturnsLoginFailed() {
        StaffAccountEntity staffAccount = new StaffAccountEntity();
        staffAccount.setId(2L);
        staffAccount.setUsername("operator");
        staffAccount.setPasswordHash(new BCryptPasswordEncoder().encode("right-password"));
        staffAccount.setStatus(1);
        staffAccount.setIsDeleted(0);

        loginRequest.setUsername("operator");
        loginRequest.setPassword("wrong-password");
        when(staffAccountMapper.selectOne(any())).thenReturn(staffAccount);

        BizException exception = assertThrows(BizException.class, () -> authService.login(loginRequest));

        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
        assertNotEquals("账号已被封禁", exception.getMessage());
    }

    @Test
    @DisplayName("enabled staff with role logs in as staff role")
    void loginStaffSuccess() {
        StaffAccountEntity staffAccount = new StaffAccountEntity();
        staffAccount.setId(2L);
        staffAccount.setUsername("operator");
        staffAccount.setDisplayName("Operator");
        staffAccount.setPasswordHash(new BCryptPasswordEncoder().encode("123456"));
        staffAccount.setStatus(1);
        staffAccount.setIsDeleted(0);

        StaffRoleRelEntity role = new StaffRoleRelEntity();
        role.setStaffAccountId(2L);
        role.setRoleCode("OPERATOR");
        role.setIsDeleted(0);

        when(staffAccountMapper.selectOne(any())).thenReturn(staffAccount);
        when(staffRoleRelMapper.selectList(any())).thenReturn(Collections.singletonList(role));

        AuthContext.LoginUser result = authService.login(loginRequest);

        assertEquals(RoleType.OPERATOR, result.getRole());
        assertEquals(2L, result.getAccountId());
    }
}
