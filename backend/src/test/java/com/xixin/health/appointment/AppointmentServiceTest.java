package com.xixin.health.appointment;

import com.xixin.health.appointment.dto.CreateAppointmentRequest;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService 单元测试")
class AppointmentServiceTest {

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private ExamPackageMapper examPackageMapper;

    @Mock
    private ResourceCapacityMapper resourceCapacityMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    private CreateAppointmentRequest createRequest;
    private ResourceCapacityEntity capacity;
    private ExamPackageEntity packageEntity;

    @BeforeEach
    void setUp() {
        createRequest = new CreateAppointmentRequest();
        createRequest.setPackageId(1001L);
        createRequest.setCenterCode("C001");
        createRequest.setAppointDate(LocalDate.now().plusDays(1));
        createRequest.setTimeSlotCode("AM01");

        capacity = new ResourceCapacityEntity();
        capacity.setId(1L);
        capacity.setCenterCode("C001");
        capacity.setCapacityTotal(20);
        capacity.setCapacityUsed(5);
        capacity.setCapacityLocked(0);
        capacity.setStatus(1);

        packageEntity = new ExamPackageEntity();
        packageEntity.setId(1001L);
        packageEntity.setPackageName("入职体检套餐");
        packageEntity.setStatus(1);
    }

    @Test
    @DisplayName("创建预约成功")
    void create_Success() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            when(examPackageMapper.selectById(1001L)).thenReturn(packageEntity);
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            when(resourceCapacityMapper.selectOne(any())).thenReturn(capacity);
            when(appointmentMapper.insert(any())).thenReturn(1);
            when(resourceCapacityMapper.update(any(), any())).thenReturn(1);

            Map<String, Object> result = appointmentService.create(createRequest);

            assertNotNull(result);
            assertNotNull(result.get("appointmentNo"));
            verify(appointmentMapper).insert(any());
        }
    }

    @Test
    @DisplayName("套餐不存在创建失败")
    void create_PackageNotFound() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            when(examPackageMapper.selectById(1001L)).thenReturn(null);

            assertThrows(BizException.class, () -> appointmentService.create(createRequest));
        }
    }

    @Test
    @DisplayName("时段已满创建失败")
    void create_CapacityFull() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);

            capacity.setCapacityUsed(20); // 已满

            when(examPackageMapper.selectById(1001L)).thenReturn(packageEntity);
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            when(resourceCapacityMapper.selectOne(any())).thenReturn(capacity);

            assertThrows(BizException.class, () -> appointmentService.create(createRequest));
        }
    }
}
