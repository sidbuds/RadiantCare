package com.xixin.health.report;

import com.xixin.health.TestMybatisPlusSupport;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.mapper.ExamReportItemMapper;
import com.xixin.health.report.mapper.ExamReportMapper;
import com.xixin.health.report.service.ReportPdfService;
import com.xixin.health.report.service.ReportPdfStorageService;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportPdfService tests")
class ReportPdfServiceTest {

    @Mock
    private ExamReportMapper examReportMapper;

    @Mock
    private ExamReportItemMapper examReportItemMapper;

    @Mock
    private ReportPdfStorageService storageService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private ExamPackageMapper examPackageMapper;

    @Mock
    private ExamCenterMapper examCenterMapper;

    @BeforeEach
    void setUp() {
        TestMybatisPlusSupport.initTableInfo(ExamReportEntity.class);
    }

    @Test
    @DisplayName("published report rebuilds current version PDF")
    void generateRebuildsCurrentVersionPdf() {
        ReportPdfService service = service();
        ExamReportEntity report = publishedReport();
        report.setPdfObjectKey("reports/7/RPT001/v1.pdf");
        report.setPdfUrl("/api/reports/RPT001/pdf/download");
        when(examReportMapper.selectOne(any())).thenReturn(report);

        ReportPdfService.ReportPdfResult result = service.generateForOwner("RPT001", 7L);

        assertEquals("reports/7/RPT001/v1.pdf", result.getObjectKey());
        assertEquals("/api/reports/RPT001/pdf/download", result.getPdfUrl());
        verify(storageService).upload(any(), any(), any(), any());
        verify(examReportMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("unpublished report cannot generate PDF")
    void generateRejectsUnpublishedReport() {
        ReportPdfService service = service();
        ExamReportEntity report = publishedReport();
        report.setStatus(1);
        when(examReportMapper.selectOne(any())).thenReturn(report);

        BizException exception = assertThrows(BizException.class, () -> service.generateForOwner("RPT001", 7L));

        assertEquals(ErrorCode.OPERATION_CONFLICT.getCode(), exception.getCode());
        verify(storageService, never()).upload(any(), any(), any(), any());
    }

    @Test
    @DisplayName("download validates owner and returns storage stream")
    void downloadValidatesOwner() {
        ReportPdfService service = service();
        ExamReportEntity report = publishedReport();
        report.setPdfObjectKey("reports/7/RPT001/v1.pdf");
        when(examReportMapper.selectOne(any())).thenReturn(report);
        ReportPdfStorageService.StoredObject stored = new ReportPdfStorageService.StoredObject(
                new ByteArrayInputStream(new byte[]{1, 2, 3}), 3L, "application/pdf");
        when(storageService.download("reports/7/RPT001/v1.pdf")).thenReturn(stored);

        ReportPdfStorageService.StoredObject result = service.downloadForOwner("RPT001", 7L);

        assertSame(stored, result);
    }

    @Test
    @DisplayName("precheck reports missing user basic fields")
    void precheckReportsMissingUserBasicFields() {
        ReportPdfService service = service();
        ExamReportEntity report = publishedReport();
        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setName("张三");
        user.setMobile("13800000000");
        when(examReportMapper.selectOne(any())).thenReturn(report);
        when(userMapper.selectById(7L)).thenReturn(user);

        ReportPdfService.ReportPdfPrecheck result = service.precheckForOwner("RPT001", 7L);

        assertEquals(false, result.isComplete());
        org.junit.jupiter.api.Assertions.assertTrue(result.getMissingFields().contains("性别"));
        org.junit.jupiter.api.Assertions.assertTrue(result.getMissingFields().contains("身份证号"));
    }

    private ExamReportEntity publishedReport() {
        ExamReportEntity report = new ExamReportEntity();
        report.setId(100L);
        report.setReportNo("RPT001");
        report.setUserId(7L);
        report.setStatus(3);
        report.setVersionNo(1);
        return report;
    }

    private ReportPdfService service() {
        return new ReportPdfService(
                examReportMapper,
                examReportItemMapper,
                storageService,
                userMapper,
                appointmentMapper,
                examPackageMapper,
                examCenterMapper
        );
    }
}
