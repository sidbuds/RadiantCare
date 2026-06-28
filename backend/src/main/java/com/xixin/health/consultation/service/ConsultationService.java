package com.xixin.health.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.consultation.dto.AssignConsultationRequest;
import com.xixin.health.consultation.dto.CreateConsultationRequest;
import com.xixin.health.consultation.dto.ReplyConsultationRequest;
import com.xixin.health.consultation.entity.DoctorConsultationEntity;
import com.xixin.health.consultation.entity.DoctorConsultationReplyEntity;
import com.xixin.health.consultation.mapper.DoctorConsultationMapper;
import com.xixin.health.consultation.mapper.DoctorConsultationReplyMapper;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.mapper.ExamReportMapper;
import com.xixin.health.report.service.ReportPdfService;
import com.xixin.health.report.service.ReportPdfStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ConsultationService {

    private final DoctorConsultationMapper doctorConsultationMapper;
    private final DoctorConsultationReplyMapper doctorConsultationReplyMapper;
    private final ExamReportMapper examReportMapper;
    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final DoctorDepartmentRelMapper doctorDepartmentRelMapper;
    private final ReportPdfService reportPdfService;
    private final AppointmentMapper appointmentMapper;

    public ConsultationService(DoctorConsultationMapper doctorConsultationMapper,
                               DoctorConsultationReplyMapper doctorConsultationReplyMapper,
                               ExamReportMapper examReportMapper,
                               StaffAccountMapper staffAccountMapper,
                               StaffRoleRelMapper staffRoleRelMapper,
                               DoctorDepartmentRelMapper doctorDepartmentRelMapper,
                               ReportPdfService reportPdfService,
                               AppointmentMapper appointmentMapper) {
        this.doctorConsultationMapper = doctorConsultationMapper;
        this.doctorConsultationReplyMapper = doctorConsultationReplyMapper;
        this.examReportMapper = examReportMapper;
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.doctorDepartmentRelMapper = doctorDepartmentRelMapper;
        this.reportPdfService = reportPdfService;
        this.appointmentMapper = appointmentMapper;
    }

    @Transactional
    public Map<String, Object> create(CreateConsultationRequest request) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, request.getReportNo())
                .eq(ExamReportEntity::getUserId, AuthContext.getUserId())
                .eq(ExamReportEntity::getStatus, 3)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在或未发布");
        }

        String targetDepartment = determineTargetDepartment(request);
        String centerCode = resolveReportCenterCode(report);
        StaffAccountEntity doctor = selectBestDoctor(targetDepartment, centerCode, request);

        DoctorConsultationEntity consultation = new DoctorConsultationEntity();
        consultation.setConsultationNo(NoGenerator.next("DC"));
        consultation.setUserId(AuthContext.getUserId());
        consultation.setDoctorId(doctor.getId());
        consultation.setDoctorName(doctor.getDisplayName());
        consultation.setDepartmentCode(targetDepartment);
        consultation.setDepartmentName(resolveDepartmentName(doctor, centerCode, targetDepartment));
        consultation.setReportId(report.getId());
        consultation.setReportNo(report.getReportNo());
        consultation.setSourceType(request.getSourceType());
        consultation.setConsultationType(request.getConsultationType());
        consultation.setConsultationTitle(request.getConsultationTitle());
        consultation.setConsultationContent(request.getConsultationContent());
        consultation.setConsultationStatus(0);
        consultation.setPriorityLevel(request.getPriorityLevel() == null ? 0 : request.getPriorityLevel());
        consultation.setHealthProfileShared(1);
        consultation.setCreatedBy(AuthContext.getUserId());
        consultation.setUpdatedBy(AuthContext.getUserId());
        consultation.setIsDeleted(0);
        doctorConsultationMapper.insert(consultation);

        insertReply(consultation.getId(), consultation.getConsultationNo(), "USER",
                AuthContext.getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getUsername(),
                request.getConsultationContent(), null, "TEXT", null);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultation.getConsultationNo());
        result.put("reportNo", consultation.getReportNo());
        result.put("status", "PENDING");
        result.put("doctorId", consultation.getDoctorId());
        result.put("doctorName", consultation.getDoctorName());
        result.put("departmentCode", consultation.getDepartmentCode());
        return result;
    }

    public List<DoctorConsultationEntity> mine() {
        return doctorConsultationMapper.selectList(new LambdaQueryWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getUserId, AuthContext.getUserId())
                .eq(DoctorConsultationEntity::getIsDeleted, 0)
                .orderByDesc(DoctorConsultationEntity::getId));
    }

    public Map<String, Object> userDetail(String consultationNo) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getUserId().equals(consultation.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return buildDetail(consultation);
    }

    @Transactional
    public Map<String, Object> closeByUser(String consultationNo) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getUserId().equals(consultation.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (consultation.getConsultationStatus() != null && consultation.getConsultationStatus() == 3) {
            throw new BizException(ErrorCode.CONSULTATION_STATUS_INVALID);
        }

        LocalDateTime now = LocalDateTime.now();
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getConsultationStatus, 3)
                .set(DoctorConsultationEntity::getClosedTime, now)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getUserId()));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultationNo);
        result.put("status", "CLOSED");
        result.put("closedTime", now.toString());
        return result;
    }

    @Transactional
    public Map<String, Object> userMessage(String consultationNo, ReplyConsultationRequest request) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getUserId().equals(consultation.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        ensureConsultationOpen(consultation);

        String messageType = normalizeMessageType(request.getMessageType());
        if ("REPORT_PDF".equals(messageType)) {
            return sendReportPdfMessage(consultation, request);
        }
        if (!hasText(request.getReplyContent())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "消息内容不能为空");
        }
        insertReply(consultation.getId(), consultation.getConsultationNo(), "USER",
                AuthContext.getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getUsername(),
                request.getReplyContent(), request.getAttachmentUrl(), "TEXT", null);

        LocalDateTime now = LocalDateTime.now();
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getConsultationStatus, 1)
                .set(DoctorConsultationEntity::getLatestReplyTime, now)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getUserId()));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultationNo);
        result.put("status", "WAITING_DOCTOR");
        result.put("messageTime", now.toString());
        return result;
    }

    public List<DoctorConsultationEntity> doctorTodo() {
        return doctorConsultationMapper.selectList(new LambdaQueryWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getDoctorId, AuthContext.getAccountId())
                .eq(DoctorConsultationEntity::getIsDeleted, 0)
                .in(DoctorConsultationEntity::getConsultationStatus, 0, 1)
                .orderByDesc(DoctorConsultationEntity::getPriorityLevel)
                .orderByAsc(DoctorConsultationEntity::getId));
    }

    public Map<String, Object> doctorDetail(String consultationNo) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getAccountId().equals(consultation.getDoctorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return buildDetail(consultation);
    }

    @Transactional
    public Map<String, Object> reply(String consultationNo, ReplyConsultationRequest request) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getAccountId().equals(consultation.getDoctorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        ensureConsultationOpen(consultation);
        if (!hasText(request.getReplyContent())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "回复内容不能为空");
        }

        insertReply(consultation.getId(), consultation.getConsultationNo(), "DOCTOR",
                AuthContext.getAccountId(),
                AuthContext.get() == null ? null : AuthContext.get().getUsername(),
                request.getReplyContent(), request.getAttachmentUrl(), "TEXT", null);

        LocalDateTime now = LocalDateTime.now();
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getConsultationStatus, 2)
                .set(DoctorConsultationEntity::getLatestReplyTime, now)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getAccountId()));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultationNo);
        result.put("status", "REPLIED");
        result.put("replyTime", now.toString());
        return result;
    }

    public ReportPdfStorageService.StoredObject downloadSharedReportPdf(String consultationNo, String reportNo) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getAccountId().equals(consultation.getDoctorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (!reportNo.equals(consultation.getReportNo()) && !hasSharedReportMessage(consultation.getId(), reportNo)) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "用户尚未在该咨询中发送此报告");
        }
        return reportPdfService.downloadForDoctor(reportNo, consultation.getUserId());
    }

    @Transactional
    public Map<String, Object> assign(String consultationNo, AssignConsultationRequest request) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        ensureConsultationOpen(consultation);

        StaffAccountEntity doctor = getEnabledDoctor(request.getDoctorId());
        String centerCode = resolveConsultationCenterCode(consultation);
        ensureDoctorMatchesConsultation(doctor.getId(), centerCode, consultation.getDepartmentCode());
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getDoctorId, doctor.getId())
                .set(DoctorConsultationEntity::getDoctorName, doctor.getDisplayName())
                .set(DoctorConsultationEntity::getDepartmentCode, consultation.getDepartmentCode())
                .set(DoctorConsultationEntity::getDepartmentName, resolveDepartmentName(doctor, centerCode, consultation.getDepartmentCode()))
                .set(DoctorConsultationEntity::getConsultationStatus, 1)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getAccountId()));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultationNo);
        result.put("doctorId", doctor.getId());
        result.put("doctorName", doctor.getDisplayName());
        result.put("status", "ASSIGNED");
        return result;
    }

    private Map<String, Object> buildDetail(DoctorConsultationEntity consultation) {
        List<DoctorConsultationReplyEntity> replies = doctorConsultationReplyMapper.selectList(
                new LambdaQueryWrapper<DoctorConsultationReplyEntity>()
                        .eq(DoctorConsultationReplyEntity::getConsultationId, consultation.getId())
                        .eq(DoctorConsultationReplyEntity::getIsDeleted, 0)
                        .orderByAsc(DoctorConsultationReplyEntity::getReplyTime)
                        .orderByAsc(DoctorConsultationReplyEntity::getId));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultation", consultation);
        result.put("replies", replies);
        return result;
    }

    private DoctorConsultationEntity getByNo(String consultationNo) {
        DoctorConsultationEntity consultation = doctorConsultationMapper.selectOne(
                new LambdaQueryWrapper<DoctorConsultationEntity>()
                        .eq(DoctorConsultationEntity::getConsultationNo, consultationNo)
                        .eq(DoctorConsultationEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (consultation == null) {
            throw new BizException(ErrorCode.CONSULTATION_NOT_FOUND);
        }
        return consultation;
    }

    private void insertReply(Long consultationId,
                             String consultationNo,
                             String role,
                             Long replyUserId,
                             String replyUserName,
                             String replyContent,
                             String attachmentUrl,
                             String messageType,
                             String refReportNo) {
        DoctorConsultationReplyEntity reply = new DoctorConsultationReplyEntity();
        reply.setConsultationId(consultationId);
        reply.setConsultationNo(consultationNo);
        reply.setReplyRole(role);
        reply.setReplyUserId(replyUserId);
        reply.setReplyUserName(replyUserName);
        reply.setReplyContent(replyContent);
        reply.setAttachmentUrl(attachmentUrl);
        reply.setMessageType(messageType);
        reply.setRefReportNo(refReportNo);
        reply.setReplyTime(LocalDateTime.now());
        reply.setCreatedBy(replyUserId);
        reply.setUpdatedBy(replyUserId);
        reply.setIsDeleted(0);
        doctorConsultationReplyMapper.insert(reply);
    }

    private Map<String, Object> sendReportPdfMessage(DoctorConsultationEntity consultation, ReplyConsultationRequest request) {
        if (!hasText(request.getRefReportNo())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "请选择要发送的报告");
        }
        ReportPdfService.ReportPdfResult pdf = reportPdfService.ensurePdfForConsultation(request.getRefReportNo(), AuthContext.getUserId());
        String content = hasText(request.getReplyContent())
                ? request.getReplyContent()
                : "我发送了一份体检报告：" + request.getRefReportNo();

        insertReply(consultation.getId(), consultation.getConsultationNo(), "USER",
                AuthContext.getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getUsername(),
                content,
                pdf.getPdfUrl(),
                "REPORT_PDF",
                request.getRefReportNo());

        LocalDateTime now = LocalDateTime.now();
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getConsultationStatus, 1)
                .set(DoctorConsultationEntity::getLatestReplyTime, now)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getUserId()));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultation.getConsultationNo());
        result.put("status", "WAITING_DOCTOR");
        result.put("messageTime", now.toString());
        result.put("reportNo", request.getRefReportNo());
        return result;
    }

    private boolean hasSharedReportMessage(Long consultationId, String reportNo) {
        Long count = doctorConsultationReplyMapper.selectCount(new LambdaQueryWrapper<DoctorConsultationReplyEntity>()
                .eq(DoctorConsultationReplyEntity::getConsultationId, consultationId)
                .eq(DoctorConsultationReplyEntity::getMessageType, "REPORT_PDF")
                .eq(DoctorConsultationReplyEntity::getRefReportNo, reportNo)
                .eq(DoctorConsultationReplyEntity::getReplyRole, "USER")
                .eq(DoctorConsultationReplyEntity::getIsDeleted, 0));
        return count != null && count > 0;
    }

    private String normalizeMessageType(String messageType) {
        return hasText(messageType) ? messageType.trim().toUpperCase(Locale.ROOT) : "TEXT";
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private void ensureConsultationOpen(DoctorConsultationEntity consultation) {
        if (consultation.getConsultationStatus() != null && consultation.getConsultationStatus() == 3) {
            throw new BizException(ErrorCode.CONSULTATION_STATUS_INVALID);
        }
    }

    private String determineTargetDepartment(CreateConsultationRequest request) {
        String type = normalizeText(request.getConsultationType());
        if (containsAny(type, "abnormal", "report_interpretation", "recheck", "检验", "化验", "指标")) {
            return "LAB";
        }

        String content = normalizeText(request.getConsultationTitle()) + " " + normalizeText(request.getConsultationContent());
        if (containsAny(content, "影像", "ct", "核磁", "b超", "彩超", "胸片", "放射")) {
            return "IMAGING";
        }
        if (containsAny(content, "心电", "心脏", "血压", "cardio", "ecg")) {
            return "CARDIO";
        }
        if (containsAny(content, "尿酸", "肾", "renal", "kidney")) {
            return "NEPHRO";
        }
        if (containsAny(content, "肝", "转氨酶", "胆红素", "liver")) {
            return "HEPATIC";
        }
        return "LAB";
    }

    private StaffAccountEntity selectBestDoctor(String targetDepartment, String centerCode, CreateConsultationRequest request) {
        List<StaffAccountEntity> doctors = enabledDoctors(centerCode, targetDepartment);
        if (doctors.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "暂无可分配医生：" + centerCode + "/" + targetDepartment);
        }

        StaffAccountEntity selected = null;
        DoctorScore selectedScore = null;
        for (StaffAccountEntity doctor : doctors) {
            DoctorScore score = buildDoctorScore(doctor, targetDepartment, request);
            if (selected == null || score.betterThan(selectedScore)) {
                selected = doctor;
                selectedScore = score;
            }
        }
        return selected;
    }

    private DoctorScore buildDoctorScore(StaffAccountEntity doctor, String targetDepartment, CreateConsultationRequest request) {
        long activeCount = doctorConsultationMapper.selectCount(new LambdaQueryWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getDoctorId, doctor.getId())
                .in(DoctorConsultationEntity::getConsultationStatus, 0, 1)
                .eq(DoctorConsultationEntity::getIsDeleted, 0));
        long openCount = doctorConsultationMapper.selectCount(new LambdaQueryWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getDoctorId, doctor.getId())
                .in(DoctorConsultationEntity::getConsultationStatus, 0, 1, 2)
                .eq(DoctorConsultationEntity::getIsDeleted, 0));

        int departmentScore = departmentMatchScore(doctor, targetDepartment);
        int specialtyScore = specialtyScore(doctor, request);
        return new DoctorScore(departmentScore + specialtyScore, activeCount, openCount, doctor.getId());
    }

    private int departmentMatchScore(StaffAccountEntity doctor, String targetDepartment) {
        if (targetDepartment == null || targetDepartment.isEmpty()) {
            return 0;
        }
        Long count = doctorDepartmentRelMapper.selectCount(new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                .eq(DoctorDepartmentRelEntity::getDoctorId, doctor.getId())
                .eq(DoctorDepartmentRelEntity::getDepartmentCode, targetDepartment)
                .eq(DoctorDepartmentRelEntity::getIsDeleted, 0));
        return count != null && count > 0 ? 100 : 0;
    }

    private int specialtyScore(StaffAccountEntity doctor, CreateConsultationRequest request) {
        String question = normalizeText(request.getConsultationType()) + " "
                + normalizeText(request.getConsultationTitle()) + " "
                + normalizeText(request.getConsultationContent());
        String doctorText = normalizeText(doctor.getUsername()) + " "
                + normalizeText(doctor.getDisplayName()) + " "
                + normalizeText(doctor.getSpecialty()) + " "
                + normalizeText(doctor.getDepartmentName());

        int score = 0;
        score += keywordScore(question, doctorText,
                new String[]{"heart", "cardio", "心", "血压", "心电"},
                new String[]{"heart", "cardio", "心内", "心血管", "心电"});
        score += keywordScore(question, doctorText,
                new String[]{"sugar", "glucose", "diabetes", "血糖", "糖尿"},
                new String[]{"sugar", "glucose", "内分泌", "糖尿", "血糖"});
        score += keywordScore(question, doctorText,
                new String[]{"liver", "肝", "转氨酶", "胆红素"},
                new String[]{"liver", "肝病", "消化", "肝"});
        score += keywordScore(question, doctorText,
                new String[]{"kidney", "renal", "肾", "尿酸", "肌酐", "尿检"},
                new String[]{"kidney", "renal", "肾内", "泌尿", "肾"});
        score += keywordScore(question, doctorText,
                new String[]{"image", "ct", "b超", "彩超", "影像", "胸片"},
                new String[]{"image", "影像", "放射", "超声"});
        score += keywordScore(question, doctorText,
                new String[]{"blood", "血常规", "白细胞", "红细胞", "血红蛋白"},
                new String[]{"blood", "检验", "血液"});
        return score;
    }

    private int keywordScore(String question, String doctorText, String[] questionKeywords, String[] doctorKeywords) {
        boolean questionMatched = false;
        for (String keyword : questionKeywords) {
            if (question.contains(keyword)) {
                questionMatched = true;
                break;
            }
        }
        if (!questionMatched) {
            return 0;
        }
        for (String keyword : doctorKeywords) {
            if (doctorText.contains(keyword)) {
                return 20;
            }
        }
        return 2;
    }

    private boolean containsAny(String source, String... values) {
        if (source == null || source.isEmpty()) {
            return false;
        }
        for (String value : values) {
            if (value != null && !value.isEmpty() && source.contains(value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private StaffAccountEntity getEnabledDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "医生ID不能为空");
        }
        StaffAccountEntity doctor = staffAccountMapper.selectOne(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getId, doctorId)
                .eq(StaffAccountEntity::getStatus, 1)
                .eq(StaffAccountEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (doctor == null || !isDoctorAccount(doctor.getId())) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "医生不存在或不可用");
        }
        return doctor;
    }

    private List<StaffAccountEntity> enabledDoctors(String centerCode, String departmentCode) {
        List<DoctorDepartmentRelEntity> rels = doctorDepartmentRelMapper.selectList(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getCenterCode, centerCode)
                        .eq(DoctorDepartmentRelEntity::getDepartmentCode, departmentCode)
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .orderByDesc(DoctorDepartmentRelEntity::getIsPrimary));
        List<StaffAccountEntity> doctors = new ArrayList<StaffAccountEntity>();
        for (DoctorDepartmentRelEntity rel : rels) {
            StaffAccountEntity account = staffAccountMapper.selectById(rel.getDoctorId());
            if (account != null
                    && Integer.valueOf(1).equals(account.getStatus())
                    && Integer.valueOf(0).equals(account.getIsDeleted())
                    && isDoctorAccount(account.getId())) {
                doctors.add(account);
            }
        }
        return doctors;
    }

    private boolean isDoctorAccount(Long accountId) {
        Long count = staffRoleRelMapper.selectCount(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, accountId)
                .eq(StaffRoleRelEntity::getRoleCode, "DOCTOR")
                .eq(StaffRoleRelEntity::getIsDeleted, 0));
        return count != null && count > 0;
    }

    private String resolveReportCenterCode(ExamReportEntity report) {
        if (report.getAppointmentId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "报告缺少预约信息，无法按中心分配咨询医生");
        }
        AppointmentEntity appointment = appointmentMapper.selectById(report.getAppointmentId());
        if (appointment == null || !hasText(appointment.getCenterCode())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "预约缺少体检中心，无法按中心分配咨询医生");
        }
        return appointment.getCenterCode();
    }

    private String resolveConsultationCenterCode(DoctorConsultationEntity consultation) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, consultation.getReportNo())
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在");
        }
        return resolveReportCenterCode(report);
    }

    private void ensureDoctorMatchesConsultation(Long doctorId, String centerCode, String departmentCode) {
        Long count = doctorDepartmentRelMapper.selectCount(new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                .eq(DoctorDepartmentRelEntity::getDoctorId, doctorId)
                .eq(DoctorDepartmentRelEntity::getCenterCode, centerCode)
                .eq(DoctorDepartmentRelEntity::getDepartmentCode, departmentCode)
                .eq(DoctorDepartmentRelEntity::getIsDeleted, 0));
        if (count == null || count == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "医生未绑定该咨询对应的中心/科室，不能分配");
        }
    }

    private String resolveDepartmentName(StaffAccountEntity doctor, String centerCode, String targetDepartment) {
        if (targetDepartment == null || targetDepartment.isEmpty()) {
            return doctor.getDepartmentName();
        }
        DoctorDepartmentRelEntity relation = doctorDepartmentRelMapper.selectOne(new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                .eq(DoctorDepartmentRelEntity::getDoctorId, doctor.getId())
                .eq(DoctorDepartmentRelEntity::getCenterCode, centerCode)
                .eq(DoctorDepartmentRelEntity::getDepartmentCode, targetDepartment)
                .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                .orderByDesc(DoctorDepartmentRelEntity::getIsPrimary)
                .last("limit 1"));
        return relation == null ? doctor.getDepartmentName() : relation.getDepartmentName();
    }

    private static class DoctorScore {
        private final int specialtyScore;
        private final long activeCount;
        private final long openCount;
        private final Long doctorId;

        private DoctorScore(int specialtyScore, long activeCount, long openCount, Long doctorId) {
            this.specialtyScore = specialtyScore;
            this.activeCount = activeCount;
            this.openCount = openCount;
            this.doctorId = doctorId;
        }

        private boolean betterThan(DoctorScore other) {
            if (other == null) {
                return true;
            }
            if (specialtyScore != other.specialtyScore) {
                return specialtyScore > other.specialtyScore;
            }
            if (activeCount != other.activeCount) {
                return activeCount < other.activeCount;
            }
            if (openCount != other.openCount) {
                return openCount < other.openCount;
            }
            return doctorId != null && other.doctorId != null && doctorId < other.doctorId;
        }
    }
}
