package com.xixin.health.exam;

import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import com.xixin.health.exam.service.DoctorAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorAssignmentService tests")
class DoctorAssignmentServiceTest {

    @Mock
    private StaffAccountMapper staffAccountMapper;

    @Mock
    private StaffRoleRelMapper staffRoleRelMapper;

    @Mock
    private ExamTaskItemMapper examTaskItemMapper;

    @Mock
    private DoctorDepartmentRelMapper doctorDepartmentRelMapper;

    @Test
    @DisplayName("doctor assignment does not fallback to staff default center department")
    void assignDoctorDoesNotFallbackToStaffDefaultFields() {
        DoctorAssignmentService service = service();
        when(doctorDepartmentRelMapper.selectList(any())).thenReturn(Collections.emptyList());

        BizException exception = assertThrows(BizException.class,
                () -> service.assignDoctor("LAB", "C001"));

        assertEquals(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(), exception.getCode());
        verify(staffAccountMapper, never()).selectList(any());
    }

    private DoctorAssignmentService service() {
        return new DoctorAssignmentService(staffAccountMapper, staffRoleRelMapper, examTaskItemMapper, doctorDepartmentRelMapper);
    }
}
