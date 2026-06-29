package com.xixin.health.appointment;

import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.appointment.dto.CreateAppointmentRequest;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ResourceCapacityEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.appointment.mapper.ResourceCapacityMapper;
import com.xixin.health.appointment.service.AppointmentService;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.SystemConfigService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.operator.service.OperatorPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService 单元测试")
class AppointmentServiceTest {

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private ExamPackageMapper examPackageMapper;

    @Mock
    private ResourceCapacityMapper resourceCapacityMapper;

    @Mock
    private ExamTaskMapper examTaskMapper;

    @Mock
    private OperatorPackageService operatorPackageService;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private AppointmentService appointmentService;

    private CreateAppointmentRequest createRequest;
    private ResourceCapacityEntity capacity;
    private ExamPackageEntity packageEntity;

    @BeforeEach
    void setUp() {
        TestMybatisPlusSupport.initTableInfo(ResourceCapacityEntity.class);
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
            mockCommonSuccessBeforeCapacity();
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            when(resourceCapacityMapper.selectList(any())).thenReturn(Collections.singletonList(capacity));
            when(appointmentMapper.insert(any())).thenReturn(1);
            when(resourceCapacityMapper.update(any(), any())).thenReturn(1);

            Map<String, Object> result = appointmentService.create(createRequest);

            assertNotNull(result);
            assertNotNull(result.get("appointmentNo"));
            verify(appointmentMapper).insert(any());
        }
    }

    @Test
    @DisplayName("套餐不存在时创建失败")
    void create_PackageNotFound() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);
            when(examPackageMapper.selectById(1001L)).thenReturn(null);

            assertThrows(BizException.class, () -> appointmentService.create(createRequest));
        }
    }

    @Test
    @DisplayName("不允许当天预约时创建失败")
    void create_TodayDisabled() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);
            createRequest.setAppointDate(LocalDate.now());
            mockCommonSuccessBeforeCapacity();

            assertThrows(BizException.class, () -> appointmentService.create(createRequest));
        }
    }

    @Test
    @DisplayName("超过提前预约天数时创建失败")
    void create_AfterAdvanceDays() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);
            createRequest.setAppointDate(LocalDate.now().plusDays(8));
            mockCommonSuccessBeforeCapacity();

            assertThrows(BizException.class, () -> appointmentService.create(createRequest));
        }
    }

    @Test
    @DisplayName("时段已满时创建失败")
    void create_CapacityFull() {
        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getUserId).thenReturn(1L);
            capacity.setCapacityUsed(20);
            mockCommonSuccessBeforeCapacity();
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            when(resourceCapacityMapper.selectList(any())).thenReturn(Collections.singletonList(capacity));

            assertThrows(BizException.class, () -> appointmentService.create(createRequest));
        }
    }

    private void mockCommonSuccessBeforeCapacity() {
        when(examPackageMapper.selectById(1001L)).thenReturn(packageEntity);
        when(operatorPackageService.isPackageAvailableAtCenter(1001L, "C001")).thenReturn(true);
        when(systemConfigService.getBooleanValue("appointment.allow_today", Boolean.FALSE)).thenReturn(false);
        when(systemConfigService.getIntValue("appointment.advance_days", 7)).thenReturn(7);
    }
}
