package com.xixin.health.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.enums.RoleType;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.user.dto.UpdateProfileRequest;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import com.xixin.health.user.service.UserProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService tests")
class UserProfileServiceTest {

    @Mock
    private UserMapper userMapper;

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("profile returns editable id number for current user")
    void getProfileReturnsEditableIdNo() {
        UserProfileService service = new UserProfileService(userMapper);
        UserEntity user = currentUser();
        user.setIdNo("110101199001010011");
        when(userMapper.selectOne(any())).thenReturn(user);
        AuthContext.set(new AuthContext.LoginUser(1L, 1L, "user", "User", RoleType.USER));

        Map<String, Object> profile = service.getProfile();

        assertEquals("110101199001010011", profile.get("idNo"));
        assertEquals(true, profile.get("idNoSet"));
    }

    @Test
    @DisplayName("current user can update existing id number")
    void updateProfileAllowsChangingExistingIdNo() {
        UserProfileService service = new UserProfileService(userMapper);
        UserEntity user = currentUser();
        user.setIdNo("110101199001010011");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        AuthContext.set(new AuthContext.LoginUser(1L, 1L, "user", "User", RoleType.USER));

        service.updateProfile(profileRequest("110101199001010022"));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals("110101199001010022", captor.getValue().getIdNo());
    }

    @Test
    @DisplayName("duplicate id number is rejected")
    void updateProfileRejectsDuplicateIdNo() {
        UserProfileService service = new UserProfileService(userMapper);
        UserEntity user = currentUser();
        user.setIdNo("110101199001010011");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        AuthContext.set(new AuthContext.LoginUser(1L, 1L, "user", "User", RoleType.USER));

        assertThrows(BizException.class, () -> service.updateProfile(profileRequest("110101199001010022")));
    }

    private UserEntity currentUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUserNo("user");
        user.setName("User");
        user.setMobile("13800000001");
        user.setIsDeleted(0);
        return user;
    }

    private UpdateProfileRequest profileRequest(String idNo) {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("User");
        request.setIdNo(idNo);
        return request;
    }
}
