package com.xixin.health.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.publicapi.entity.ExamCenterEntity;
import com.xixin.health.publicapi.mapper.ExamCenterMapper;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.entity.ExamReportItemEntity;
import com.xixin.health.report.mapper.ExamReportItemMapper;
import com.xixin.health.report.mapper.ExamReportMapper;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportPdfService {

    private static final int REPORT_PUBLISHED = 3;
    private static final String CONTENT_TYPE = "application/pdf";

    private final ExamReportMapper examReportMapper;
    private final ExamReportItemMapper examReportItemMapper;
    private final ReportPdfStorageService storageService;
    private final UserMapper userMapper;
    private final AppointmentMapper appointmentMapper;
    private final ExamPackageMapper examPackageMapper;
    private final ExamCenterMapper examCenterMapper;

    public ReportPdfService(ExamReportMapper examReportMapper,
                            ExamReportItemMapper examReportItemMapper,
                            ReportPdfStorageService storageService,
                            UserMapper userMapper,
                            AppointmentMapper appointmentMapper,
                            ExamPackageMapper examPackageMapper,
                            ExamCenterMapper examCenterMapper) {
        this.examReportMapper = examReportMapper;
        this.examReportItemMapper = examReportItemMapper;
        this.storageService = storageService;
        this.userMapper = userMapper;
        this.appointmentMapper = appointmentMapper;
        this.examPackageMapper = examPackageMapper;
        this.examCenterMapper = examCenterMapper;
    }

    @Transactional
    public ReportPdfResult generateForOwner(String reportNo, Long userId) {
        ExamReportEntity report = getPublishedReportForOwner(reportNo, userId);
        List<ExamReportItemEntity> items = listItems(report.getId());
        ReportBasicInfo basicInfo = buildBasicInfo(report);
        byte[] pdfBytes = renderPdf(report, items, basicInfo);
        String objectKey = buildObjectKey(report);
        String pdfUrl = "/api/reports/" + report.getReportNo() + "/pdf/download";

        storageService.upload(objectKey, pdfBytes, CONTENT_TYPE, report.getReportNo() + ".pdf");
        examReportMapper.update(null, new LambdaUpdateWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getId, report.getId())
                .set(ExamReportEntity::getPdfObjectKey, objectKey)
                .set(ExamReportEntity::getPdfUrl, pdfUrl));

        return new ReportPdfResult(report.getReportNo(), pdfUrl, objectKey);
    }

    public ReportPdfPrecheck precheckForOwner(String reportNo, Long userId) {
        ExamReportEntity report = getPublishedReportForOwner(reportNo, userId);
        ReportBasicInfo basicInfo = buildBasicInfo(report);
        List<String> missingFields = basicInfo.missingFields();
        return new ReportPdfPrecheck(report.getReportNo(), missingFields.isEmpty(), missingFields);
    }

    public ReportPdfStorageService.StoredObject downloadForOwner(String reportNo, Long userId) {
        ExamReportEntity report = getPublishedReportForOwner(reportNo, userId);
        return storageService.download(ensurePdf(report, userId).getObjectKey());
    }

    public ReportPdfStorageService.StoredObject downloadForDoctor(String reportNo, Long userId) {
        ExamReportEntity report = getPublishedReportForOwner(reportNo, userId);
        return storageService.download(ensurePdf(report, userId).getObjectKey());
    }

    public ReportPdfResult ensurePdfForConsultation(String reportNo, Long userId) {
        return generateForOwner(reportNo, userId);
    }

    private ReportPdfResult ensurePdf(ExamReportEntity report, Long userId) {
        if (hasText(report.getPdfObjectKey())) {
            String pdfUrl = hasText(report.getPdfUrl()) ? report.getPdfUrl() : "/api/reports/" + report.getReportNo() + "/pdf/download";
            return new ReportPdfResult(report.getReportNo(), pdfUrl, report.getPdfObjectKey());
        }
        return generateForOwner(report.getReportNo(), userId);
    }

    private ExamReportEntity getPublishedReportForOwner(String reportNo, Long userId) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, reportNo)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (report.getStatus() == null || report.getStatus() != REPORT_PUBLISHED) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "报告未发布，不能导出PDF");
        }
        return report;
    }

    private List<ExamReportItemEntity> listItems(Long reportId) {
        return examReportItemMapper.selectList(new LambdaQueryWrapper<ExamReportItemEntity>()
                .eq(ExamReportItemEntity::getReportId, reportId)
                .eq(ExamReportItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamReportItemEntity::getSortNo));
    }

    private String buildObjectKey(ExamReportEntity report) {
        int version = report.getVersionNo() == null ? 1 : report.getVersionNo();
        return "reports/" + report.getUserId() + "/" + report.getReportNo() + "/v" + version + ".pdf";
    }

    private ReportBasicInfo buildBasicInfo(ExamReportEntity report) {
        UserEntity user = userMapper.selectById(report.getUserId());
        AppointmentEntity appointment = report.getAppointmentId() == null ? null : appointmentMapper.selectById(report.getAppointmentId());
        ExamPackageEntity examPackage = report.getPackageId() == null ? null : examPackageMapper.selectById(report.getPackageId());
        ExamCenterEntity center = null;
        if (appointment != null && hasText(appointment.getCenterCode())) {
            center = examCenterMapper.selectOne(new LambdaQueryWrapper<ExamCenterEntity>()
                    .eq(ExamCenterEntity::getCenterCode, appointment.getCenterCode())
                    .eq(ExamCenterEntity::getIsDeleted, 0)
                    .last("limit 1"));
        }

        ReportBasicInfo info = new ReportBasicInfo();
        info.name = user == null ? null : user.getName();
        info.gender = user == null ? null : genderText(user.getGender());
        info.age = user == null ? null : ageText(user.getBirthDate());
        info.mobile = user == null ? null : user.getMobile();
        info.maskedIdNo = user == null ? null : maskIdNo(user.getIdNo());
        info.centerName = center == null ? (appointment == null ? null : appointment.getCenterCode()) : center.getCenterName();
        info.packageName = examPackage == null ? null : examPackage.getPackageName();
        info.examDate = appointment == null || appointment.getAppointDate() == null ? null : appointment.getAppointDate().toString();
        return info;
    }

    private byte[] renderPdf(ExamReportEntity report, List<ExamReportItemEntity> items, ReportBasicInfo basicInfo) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = chineseFont(20, Font.BOLD);
            Font sectionFont = chineseFont(13, Font.BOLD);
            Font normalFont = chineseFont(10, Font.NORMAL);
            Font smallFont = chineseFont(9, Font.NORMAL);

            Paragraph title = new Paragraph("熙心健康体检报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12);
            document.add(title);

            addKeyValueTable(document, normalFont, new String[][]{
                    {"报告编号", safe(report.getReportNo())},
                    {"报告日期", report.getReportDate() == null ? "-" : report.getReportDate().toString()},
                    {"报告版本", "v" + (report.getVersionNo() == null ? 1 : report.getVersionNo())},
                    {"生成时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}
            });

            addSection(document, "基础信息", sectionFont);
            addKeyValueTable(document, normalFont, new String[][]{
                    {"姓名", safe(basicInfo.name)},
                    {"性别", safe(basicInfo.gender)},
                    {"年龄", safe(basicInfo.age)},
                    {"手机号", safe(basicInfo.mobile)},
                    {"身份证号", safe(basicInfo.maskedIdNo)},
                    {"体检中心", safe(basicInfo.centerName)},
                    {"套餐名称", safe(basicInfo.packageName)},
                    {"体检日期", safe(basicInfo.examDate)}
            });

            addSection(document, "总体结论", sectionFont);
            document.add(new Paragraph(safe(report.getOverallConclusion(), "本次体检报告已生成，请结合医生建议查看各项指标。"), normalFont));

            List<ExamReportItemEntity> abnormalItems = filterAbnormal(items);
            if (!abnormalItems.isEmpty()) {
                addSection(document, "异常指标汇总", sectionFont);
                document.add(buildItemsTable(abnormalItems, normalFont, true));
            }

            addSection(document, "检查结果总览", sectionFont);
            if (items.isEmpty()) {
                document.add(new Paragraph("暂无检查项目明细。", normalFont));
            } else {
                document.add(buildItemsTable(items, normalFont, false));
            }

            addSection(document, "健康建议", sectionFont);
            document.add(new Paragraph(buildSuggestion(abnormalItems), normalFont));

            Paragraph footer = new Paragraph("说明：本报告依据已发布体检结果快照生成，仅供健康管理参考；如有不适请及时线下就医。", smallFont);
            footer.setSpacingBefore(16);
            document.add(footer);

            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "PDF生成失败，请稍后重试");
        }
    }

    private void addSection(Document document, String title, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(16);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
    }

    private void addKeyValueTable(Document document, Font font, String[][] rows) throws Exception {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.1f, 1.9f, 1.1f, 1.9f});
        for (String[] row : rows) {
            addCell(table, row[0], font, true);
            addCell(table, row[1], font, false);
        }
        document.add(table);
    }

    private PdfPTable buildItemsTable(List<ExamReportItemEntity> items, Font font, boolean includeLevel) throws Exception {
        PdfPTable table = includeLevel ? new PdfPTable(6) : new PdfPTable(5);
        table.setWidthPercentage(100);
        if (includeLevel) {
            table.setWidths(new float[]{1.4f, 1.3f, 0.8f, 1.4f, 0.8f, 0.8f});
            addHeader(table, font, "项目", "结果", "单位", "参考范围", "状态", "等级");
        } else {
            table.setWidths(new float[]{1.5f, 1.4f, 0.8f, 1.5f, 0.8f});
            addHeader(table, font, "项目", "结果", "单位", "参考范围", "状态");
        }
        for (ExamReportItemEntity item : items) {
            addCell(table, safe(item.getItemName(), item.getItemCode()), font, false);
            addCell(table, safe(item.getResultValue()), font, false);
            addCell(table, safe(item.getUnit()), font, false);
            addCell(table, safe(item.getRefRange()), font, false);
            addCell(table, item.getIsAbnormal() != null && item.getIsAbnormal() == 1 ? "异常" : "正常", font, false);
            if (includeLevel) {
                addCell(table, item.getAbnormalLevel() == null ? "-" : String.valueOf(item.getAbnormalLevel()), font, false);
            }
        }
        return table;
    }

    private void addHeader(PdfPTable table, Font font, String... values) {
        for (String value : values) {
            addCell(table, value, font, true);
        }
    }

    private void addCell(PdfPTable table, String value, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "-" : value, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(header ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
        if (header) {
            cell.setGrayFill(0.92f);
        }
        table.addCell(cell);
    }

    private Font chineseFont(int size, int style) {
        try {
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            return new Font(baseFont, size, style);
        } catch (Exception e) {
            return FontFactory.getFont(FontFactory.HELVETICA, size, style);
        }
    }

    private List<ExamReportItemEntity> filterAbnormal(List<ExamReportItemEntity> items) {
        ArrayList<ExamReportItemEntity> result = new ArrayList<ExamReportItemEntity>();
        for (ExamReportItemEntity item : items) {
            if (item.getIsAbnormal() != null && item.getIsAbnormal() == 1) {
                result.add(item);
            }
        }
        return result;
    }

    private String buildSuggestion(List<ExamReportItemEntity> abnormalItems) {
        if (abnormalItems.isEmpty()) {
            return "本次报告未发现明显异常指标，请继续保持规律作息、合理饮食和适量运动。";
        }
        Map<String, Boolean> names = new HashMap<String, Boolean>();
        for (ExamReportItemEntity item : abnormalItems) {
            names.put(safe(item.getItemName(), item.getItemCode()), Boolean.TRUE);
        }
        return "本次报告存在 " + abnormalItems.size() + " 项异常指标：" + String.join("、", names.keySet())
                + "。建议结合医生解读进行复查或进一步评估，近期注意饮食、作息和运动管理。";
    }

    private String genderText(Integer gender) {
        if (gender == null) return null;
        if (gender == 1) return "男";
        if (gender == 2) return "女";
        return "未知";
    }

    private String ageText(LocalDate birthDate) {
        if (birthDate == null) return null;
        return String.valueOf(Math.max(0, Period.between(birthDate, LocalDate.now()).getYears()));
    }

    private String maskIdNo(String idNo) {
        if (!hasText(idNo)) return null;
        String value = idNo.trim();
        if (value.length() <= 8) return "****";
        return value.substring(0, 4) + "**********" + value.substring(value.length() - 4);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String safe(String value) {
        return safe(value, "-");
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    @Getter
    @AllArgsConstructor
    public static class ReportPdfResult {
        private final String reportNo;
        private final String pdfUrl;
        private final String objectKey;
    }

    @Getter
    @AllArgsConstructor
    public static class ReportPdfPrecheck {
        private final String reportNo;
        private final boolean complete;
        private final List<String> missingFields;
    }

    private static class ReportBasicInfo {
        private String name;
        private String gender;
        private String age;
        private String mobile;
        private String maskedIdNo;
        private String centerName;
        private String packageName;
        private String examDate;

        private List<String> missingFields() {
            List<String> result = new ArrayList<String>();
            addMissing(result, "姓名", name);
            addMissing(result, "性别", gender);
            addMissing(result, "年龄", age);
            addMissing(result, "手机号", mobile);
            addMissing(result, "身份证号", maskedIdNo);
            addMissing(result, "体检中心", centerName);
            addMissing(result, "套餐名称", packageName);
            addMissing(result, "体检日期", examDate);
            return result;
        }

        private void addMissing(List<String> result, String label, String value) {
            if (value == null || value.trim().isEmpty()) {
                result.add(label);
            }
        }
    }
}
