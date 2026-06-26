package com.xixin.health.admin;

import com.xixin.health.admin.service.AdminRoleService;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminRoleService tests")
class AdminRoleServiceTest {

    @Mock
    private StaffAccountMapper staffAccountMapper;

    @Mock
    private StaffRoleRelMapper staffRoleRelMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("granting doctor role creates staff account and role relation")
    void grantUserRoleCreatesStaffAccountAndRole() {
        AdminRoleService service = new AdminRoleService(staffAccountMapper, staffRoleRelMapper, userMapper);
        UserEntity user = new UserEntity();
        user.setId(8L);
        user.setUserNo("u008");
        user.setName("User Eight");
        user.setPasswordHash("hash");
        user.setStatus(1);
        user.setIsDeleted(0);

        when(userMapper.selectById(8L)).thenReturn(user);
        when(staffAccountMapper.selectOne(any())).thenReturn(null);
        when(staffRoleRelMapper.selectOne(any())).thenReturn(null);

        service.grantUserRole(8L, "DOCTOR");

        ArgumentCaptor<StaffAccountEntity> accountCaptor = ArgumentCaptor.forClass(StaffAccountEntity.class);
        verify(staffAccountMapper).insert(accountCaptor.capture());
        assertEquals("u008", accountCaptor.getValue().getUsername());
        assertEquals("hash", accountCaptor.getValue().getPasswordHash());
        assertEquals(8L, accountCaptor.getValue().getBindUserId());

        ArgumentCaptor<StaffRoleRelEntity> roleCaptor = ArgumentCaptor.forClass(StaffRoleRelEntity.class);
        verify(staffRoleRelMapper).insert(roleCaptor.capture());
        assertEquals("DOCTOR", roleCaptor.getValue().getRoleCode());
    }

    @Test
    @DisplayName("granting user role only supports doctor or operator")
    void grantUserRoleRejectsUnsupportedRole() {
        AdminRoleService service = new AdminRoleService(staffAccountMapper, staffRoleRelMapper, userMapper);
        assertThrows(BizException.class, () -> service.grantUserRole(8L, "ADMIN"));
    }
}
