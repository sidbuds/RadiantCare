package com.xixin.health.ai;

import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.ai.dto.ReportAgentChatRequest;
import com.xixin.health.ai.dto.ReportAgentSendReportRequest;
import com.xixin.health.ai.entity.AiAgentSessionEntity;
import com.xixin.health.ai.entity.AiAgentSessionReportEntity;
import com.xixin.health.ai.mapper.AiAgentSessionMapper;
import com.xixin.health.ai.mapper.AiAgentSessionReportMapper;
import com.xixin.health.ai.service.ReportAgentModelClient;
import com.xixin.health.ai.service.ReportAgentService;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.SystemConfigService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.entity.ExamReportItemEntity;
import com.xixin.health.report.mapper.ExamReportItemMapper;
import com.xixin.health.report.mapper.ExamReportMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportAgentService tests")
class ReportAgentServiceTest {

    @Mock
    private AiAgentSessionMapper sessionMapper;
    @Mock
    private AiAgentSessionReportMapper sessionReportMapper;
    @Mock
    private ExamReportMapper reportMapper;
    @Mock
    private ExamReportItemMapper reportItemMapper;
    @Mock
    private ExamPackageMapper packageMapper;
    @Mock
    private ExamPackageItemMapper packageItemMapper;
    @Mock
    private SystemConfigService systemConfigService;
    @Mock
    private ReportAgentModelClient modelClient;

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    @DisplayName("agent rejects report analysis before user sends a report")
    void chatRejectsReportAnalysisBeforeReportAuthorized() {
        TestMybatisPlusSupport.initTableInfo(AiAgentSessionEntity.class, AiAgentSessionReportEntity.class);
        AuthContext.set(new AuthContext.LoginUser(1L, 100L, "user", "User", null));
        ReportAgentService service = service();
        AiAgentSessionEntity session = session();
        when(sessionMapper.selectOne(any())).thenReturn(session);
        when(sessionReportMapper.selectList(any())).thenReturn(Collections.emptyList());

        ReportAgentChatRequest request = new ReportAgentChatRequest();
        request.setQuestion("请根据我的报告分析一下");

        BizException ex = assertThrows(BizException.class, () -> service.chat("AIS001", request));

        assertEquals("请先发送报告给智能助手，再进行报告分析", ex.getMessage());
        verify(modelClient, never()).chat(any());
    }

    @Test
    @DisplayName("sending report authorizes only current user's published report")
    void sendReportAuthorizesCurrentUsersPublishedReport() {
        TestMybatisPlusSupport.initTableInfo(AiAgentSessionEntity.class, AiAgentSessionReportEntity.class, ExamReportEntity.class);
        AuthContext.set(new AuthContext.LoginUser(1L, 100L, "user", "User", null));
        ReportAgentService service = service();
        AiAgentSessionEntity session = session();
        ExamReportEntity report = report(200L, "RPT001", 100L, 3);
        when(sessionMapper.selectOne(any())).thenReturn(session);
        when(reportMapper.selectOne(any())).thenReturn(report);
        when(sessionReportMapper.selectCount(any())).thenReturn(0L);

        ReportAgentSendReportRequest request = new ReportAgentSendReportRequest();
        request.setReportNo("RPT001");
        Map<String, Object> result = service.sendReport("AIS001", request);

        assertEquals("RPT001", result.get("reportNo"));
        ArgumentCaptor<AiAgentSessionReportEntity> captor = ArgumentCaptor.forClass(AiAgentSessionReportEntity.class);
        verify(sessionReportMapper).insert(captor.capture());
        assertEquals(10L, captor.getValue().getSessionId());
        assertEquals(200L, captor.getValue().getReportId());
    }

    @Test
    @DisplayName("chat uses only authorized reports and package candidates")
    void chatUsesAuthorizedReportsAndPackageCandidates() {
        TestMybatisPlusSupport.initTableInfo(AiAgentSessionEntity.class, AiAgentSessionReportEntity.class,
                ExamReportEntity.class, ExamReportItemEntity.class, ExamPackageEntity.class, ExamPackageItemEntity.class);
        AuthContext.set(new AuthContext.LoginUser(1L, 100L, "user", "User", null));
        ReportAgentService service = service();
        AiAgentSessionEntity session = session();
        AiAgentSessionReportEntity sessionReport = new AiAgentSessionReportEntity();
        sessionReport.setReportId(200L);
        sessionReport.setReportNo("RPT001");
        when(sessionMapper.selectOne(any())).thenReturn(session);
        when(sessionReportMapper.selectList(any())).thenReturn(Collections.singletonList(sessionReport));
        when(reportMapper.selectById(200L)).thenReturn(report(200L, "RPT001", 100L, 3));
        when(reportItemMapper.selectList(any())).thenReturn(Collections.singletonList(reportItem()));
        when(packageMapper.selectList(any())).thenReturn(Collections.singletonList(packageEntity()));
        when(packageItemMapper.selectList(any())).thenReturn(Collections.singletonList(packageItem()));
        when(modelClient.chat(any())).thenReturn("{\"answer\":\"血脂偏高，建议复查。\",\"orderPitch\":\"一句话：建议预约心血管专项复查套餐。\",\"recommendedPackages\":[{\"packageId\":300,\"packageCode\":\"PKG001\",\"packageName\":\"心血管复查\",\"price\":399,\"reason\":\"覆盖血脂和心电检查\",\"actionUrl\":\"/user/appointments/create?packageId=300&packageCode=PKG001\"}],\"safetyNotice\":\"AI建议不能替代医生诊断。\"}");

        ReportAgentChatRequest request = new ReportAgentChatRequest();
        request.setQuestion("我想复查血脂，帮我推荐套餐");
        Map<String, Object> result = service.chat("AIS001", request);

        assertEquals("血脂偏高，建议复查。", result.get("answer"));
        verify(modelClient).chat(any());
    }

    @Test
    @DisplayName("chat parses fenced json and completes package action fields")
    void chatParsesFencedJsonAndCompletesPackageActionFields() {
        TestMybatisPlusSupport.initTableInfo(AiAgentSessionEntity.class, AiAgentSessionReportEntity.class,
                ExamReportEntity.class, ExamReportItemEntity.class, ExamPackageEntity.class, ExamPackageItemEntity.class);
        AuthContext.set(new AuthContext.LoginUser(1L, 100L, "user", "User", null));
        ReportAgentService service = service();
        AiAgentSessionEntity session = session();
        AiAgentSessionReportEntity sessionReport = new AiAgentSessionReportEntity();
        sessionReport.setReportId(200L);
        sessionReport.setReportNo("RPT001");
        when(sessionMapper.selectOne(any())).thenReturn(session);
        when(sessionReportMapper.selectList(any())).thenReturn(Collections.singletonList(sessionReport));
        when(reportMapper.selectById(200L)).thenReturn(report(200L, "RPT001", 100L, 3));
        when(reportItemMapper.selectList(any())).thenReturn(Collections.singletonList(reportItem()));
        when(packageMapper.selectList(any())).thenReturn(Collections.singletonList(packageEntity()));
        when(packageItemMapper.selectList(any())).thenReturn(Collections.singletonList(packageItem()));
        when(modelClient.chat(any())).thenReturn("```json\n{\"answer\":\"ok\",\"recommendedPackages\":[{\"packageId\":300,\"packageName\":\"Package\"}]}\n```");

        ReportAgentChatRequest request = new ReportAgentChatRequest();
        request.setQuestion("package advice");
        Map<String, Object> result = service.chat("AIS001", request);

        assertEquals("ok", result.get("answer"));
        List<Map<String, Object>> recommended = (List<Map<String, Object>>) result.get("recommendedPackages");
        assertEquals("PKG001", recommended.get(0).get("packageCode"));
        assertEquals("/user/appointments/create?packageId=300&packageCode=PKG001", recommended.get(0).get("actionUrl"));
        assertEquals("AI建议不能替代医生诊断，如有不适请及时咨询医生。", result.get("safetyNotice"));
    }

    private ReportAgentService service() {
        return new ReportAgentService(sessionMapper, sessionReportMapper, reportMapper, reportItemMapper,
                packageMapper, packageItemMapper, systemConfigService, modelClient);
    }

    private AiAgentSessionEntity session() {
        AiAgentSessionEntity entity = new AiAgentSessionEntity();
        entity.setId(10L);
        entity.setSessionNo("AIS001");
        entity.setUserId(100L);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        return entity;
    }

    private ExamReportEntity report(Long id, String reportNo, Long userId, Integer status) {
        ExamReportEntity entity = new ExamReportEntity();
        entity.setId(id);
        entity.setReportNo(reportNo);
        entity.setUserId(userId);
        entity.setStatus(status);
        entity.setOverallConclusion("血脂偏高");
        entity.setIsDeleted(0);
        return entity;
    }

    private ExamReportItemEntity reportItem() {
        ExamReportItemEntity item = new ExamReportItemEntity();
        item.setItemCode("TG");
        item.setItemName("甘油三酯");
        item.setResultValue("2.3");
        item.setUnit("mmol/L");
        item.setRefRange("0-1.7");
        item.setIsAbnormal(1);
        item.setAbnormalLevel(2);
        return item;
    }

    private ExamPackageEntity packageEntity() {
        ExamPackageEntity entity = new ExamPackageEntity();
        entity.setId(300L);
        entity.setPackageCode("PKG001");
        entity.setPackageName("心血管复查");
        entity.setCategory("复查");
        entity.setPrice(new BigDecimal("399.00"));
        entity.setStatus(1);
        entity.setRemark("适合血脂复查");
        entity.setIsDeleted(0);
        return entity;
    }

    private ExamPackageItemEntity packageItem() {
        ExamPackageItemEntity item = new ExamPackageItemEntity();
        item.setPackageId(300L);
        item.setItemCode("TG");
        item.setItemName("甘油三酯");
        item.setIsDeleted(0);
        return item;
    }
}
