package com.xixin.health.doctor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.enums.RoleType;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.consultation.mapper.DoctorConsultationReplyMapper;
import com.xixin.health.doctor.service.DoctorAnalyticsService;
import com.xixin.health.exam.entity.ExamResultEntity;
import com.xixin.health.exam.mapper.ExamResultMapper;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import com.xixin.health.report.mapper.DoctorReviewRecordMapper;
import com.xixin.health.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorAnalyticsService 单元测试")
class DoctorAnalyticsServiceTest {

    @Mock
    private ExamResultMapper examResultMapper;
    @Mock
    private ExamTaskItemMapper examTaskItemMapper;
    @Mock
    private DoctorReviewRecordMapper doctorReviewRecordMapper;
    @Mock
    private DoctorConsultationReplyMapper doctorConsultationReplyMapper;
    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("医生异常统计使用登录账号ID查询医生数据")
    void abnormalOverview_UsesAccountId() {
        DoctorAnalyticsService service = new DoctorAnalyticsService(
                examResultMapper,
                examTaskItemMapper,
                doctorReviewRecordMapper,
                doctorConsultationReplyMapper,
                userMapper);

        ExamResultEntity result = new ExamResultEntity();
        result.setEntryDoctorId(100L);
        result.setEntryTime(LocalDateTime.now());
        result.setIsAbnormal(1);
        result.setAbnormalLevel(2);
        when(examResultMapper.selectList(any())).thenReturn(Collections.singletonList(result));

        try (MockedStatic<AuthContext> authContext = mockStatic(AuthContext.class)) {
            authContext.when(AuthContext::getAccountId).thenReturn(100L);
            authContext.when(AuthContext::getUserId).thenReturn(1L);
            authContext.when(AuthContext::getRole).thenReturn(RoleType.DOCTOR);

            Map<String, Object> overview = service.abnormalOverview(null, null);

            assertEquals(100L, overview.get("doctorId"));
            ArgumentCaptor<LambdaQueryWrapper<ExamResultEntity>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
            verify(examResultMapper).selectList(captor.capture());
        }
    }
}
