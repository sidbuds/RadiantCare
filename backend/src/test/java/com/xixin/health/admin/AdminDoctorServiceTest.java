package com.xixin.health.admin;

import com.xixin.health.admin.service.AdminDoctorService;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDoctorService tests")
class AdminDoctorServiceTest {

    @Mock
    private StaffAccountMapper staffAccountMapper;

    @Mock
    private StaffRoleRelMapper staffRoleRelMapper;

    @Mock
    private DoctorDepartmentRelMapper doctorDepartmentRelMapper;

    @Mock
    private AuditLogService auditLogService;

    @Test
    @DisplayName("same doctor department center binding is rejected before insert")
    void bindDepartmentRejectsSameCenterDepartment() {
        AdminDoctorService service = service();
        DoctorDepartmentRelEntity existing = new DoctorDepartmentRelEntity();
        existing.setDoctorId(5L);
        existing.setDepartmentCode("LAB");
        existing.setCenterCode("C001");
        existing.setIsDeleted(0);
        when(doctorDepartmentRelMapper.selectOne(any())).thenReturn(existing);

        BizException exception = assertThrows(BizException.class,
                () -> service.bindDepartment(5L, "LAB", "Laboratory", "C001", false));

        assertEquals(ErrorCode.OPERATION_CONFLICT.getCode(), exception.getCode());
        verify(doctorDepartmentRelMapper, never()).insert(any());
    }

    @Test
    @DisplayName("doctor already bound to one center cannot bind another center")
    void bindDepartmentRejectsDifferentCenter() {
        AdminDoctorService service = service();
        when(doctorDepartmentRelMapper.selectList(any()))
                .thenReturn(Collections.singletonList(rel(1L, 5L, "C001", "LAB", "检验科", 1, 0)));

        BizException exception = assertThrows(BizException.class,
                () -> service.bindDepartment(5L, "RESP", "呼吸科", "C002", false));

        assertEquals(ErrorCode.OPERATION_CONFLICT.getCode(), exception.getCode());
        verify(doctorDepartmentRelMapper, never()).insert(any());
    }

    @Test
    @DisplayName("doctor can bind another department in the same center")
    void bindDepartmentAllowsSameCenterAnotherDepartment() {
        AdminDoctorService service = service();
        when(doctorDepartmentRelMapper.selectOne(any())).thenReturn(null);

        service.bindDepartment(5L, "RESP", "呼吸科", "C001", false);

        verify(doctorDepartmentRelMapper).insert(any(DoctorDepartmentRelEntity.class));
    }

    @Test
    @DisplayName("deleted binding is restored instead of inserted")
    void bindDepartmentRestoresDeletedBinding() {
        AdminDoctorService service = service();
        DoctorDepartmentRelEntity deleted = rel(9L, 5L, "C001", "LAB", "检验科", 0, 1);
        when(doctorDepartmentRelMapper.selectOne(any()))
                .thenReturn(null)
                .thenReturn(deleted);
        when(doctorDepartmentRelMapper.selectList(any())).thenReturn(Collections.emptyList());

        service.bindDepartment(5L, "LAB", "检验科", "C001", true);

        verify(doctorDepartmentRelMapper).updateById(deleted);
        verify(doctorDepartmentRelMapper, never()).insert(any());
    }

    @Test
    @DisplayName("updating default center department synchronizes primary binding")
    void updateDoctorSynchronizesPrimaryBinding() {
        AdminDoctorService service = service();
        StaffAccountEntity account = doctor(5L, "doctor3", "C001", "LAB");
        when(staffAccountMapper.selectById(5L)).thenReturn(account);
        when(doctorDepartmentRelMapper.selectOne(any())).thenReturn(null);
        when(doctorDepartmentRelMapper.selectList(any())).thenReturn(Collections.emptyList());

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("centerCode", "C001");
        data.put("departmentCode", "LAB");
        data.put("departmentName", "检验科");

        service.updateDoctor(5L, data);

        verify(staffAccountMapper).updateById(account);
        verify(doctorDepartmentRelMapper).insert(any(DoctorDepartmentRelEntity.class));
    }

    @Test
    @DisplayName("duplicate key during bind is converted to business conflict")
    void bindDepartmentConvertsDuplicateKey() {
        AdminDoctorService service = service();
        when(doctorDepartmentRelMapper.selectOne(any())).thenReturn(null);
        when(doctorDepartmentRelMapper.insert(any())).thenThrow(new DuplicateKeyException("duplicate"));

        BizException exception = assertThrows(BizException.class,
                () -> service.bindDepartment(5L, "LAB", "Laboratory", "C001", false));

        assertEquals(ErrorCode.OPERATION_CONFLICT.getCode(), exception.getCode());
    }

    private AdminDoctorService service() {
        return new AdminDoctorService(staffAccountMapper, staffRoleRelMapper, doctorDepartmentRelMapper, auditLogService);
    }

    private DoctorDepartmentRelEntity rel(Long id, Long doctorId, String centerCode, String departmentCode,
                                          String departmentName, Integer isPrimary, Integer isDeleted) {
        DoctorDepartmentRelEntity rel = new DoctorDepartmentRelEntity();
        rel.setId(id);
        rel.setDoctorId(doctorId);
        rel.setCenterCode(centerCode);
        rel.setDepartmentCode(departmentCode);
        rel.setDepartmentName(departmentName);
        rel.setIsPrimary(isPrimary);
        rel.setIsDeleted(isDeleted);
        return rel;
    }

    private StaffAccountEntity doctor(Long id, String username, String centerCode, String departmentCode) {
        StaffAccountEntity account = new StaffAccountEntity();
        account.setId(id);
        account.setUsername(username);
        account.setDisplayName(username);
        account.setCenterCode(centerCode);
        account.setDepartmentCode(departmentCode);
        account.setStatus(1);
        account.setIsDeleted(0);
        return account;
    }
}
