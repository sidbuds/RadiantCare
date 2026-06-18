package com.xixin.health.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultationService {

    private static final long DEFAULT_DOCTOR_ID = 2L;
    private static final String DEFAULT_DOCTOR_NAME = "doctor";

    private final DoctorConsultationMapper doctorConsultationMapper;
    private final DoctorConsultationReplyMapper doctorConsultationReplyMapper;
    private final ExamReportMapper examReportMapper;
    private final UserMapper userMapper;

    public ConsultationService(DoctorConsultationMapper doctorConsultationMapper,
                               DoctorConsultationReplyMapper doctorConsultationReplyMapper,
                               ExamReportMapper examReportMapper,
                               UserMapper userMapper) {
        this.doctorConsultationMapper = doctorConsultationMapper;
        this.doctorConsultationReplyMapper = doctorConsultationReplyMapper;
        this.examReportMapper = examReportMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public Map<String, Object> create(CreateConsultationRequest request) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, request.getReportNo())
                .eq(ExamReportEntity::getUserId, AuthContext.getUserId())
                .eq(ExamReportEntity::getStatus, 3)
                .eq(ExamReportEntity::getIsDeleted, 0));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在或未发布");
        }

        DoctorConsultationEntity consultation = new DoctorConsultationEntity();
        consultation.setConsultationNo(NoGenerator.next("DC"));
        consultation.setUserId(AuthContext.getUserId());
        consultation.setDoctorId(DEFAULT_DOCTOR_ID);
        consultation.setDoctorName(resolveUserName(DEFAULT_DOCTOR_ID, DEFAULT_DOCTOR_NAME));
        consultation.setReportId(report.getId());
        consultation.setReportNo(report.getReportNo());
        consultation.setSourceType(request.getSourceType());
        consultation.setConsultationType(request.getConsultationType());
        consultation.setConsultationTitle(request.getConsultationTitle());
        consultation.setConsultationContent(request.getConsultationContent());
        consultation.setConsultationStatus(0);
        consultation.setPriorityLevel(request.getPriorityLevel() == null ? 0 : request.getPriorityLevel());
        consultation.setCreatedBy(AuthContext.getUserId());
        consultation.setUpdatedBy(AuthContext.getUserId());
        consultation.setIsDeleted(0);
        doctorConsultationMapper.insert(consultation);

        insertReply(consultation.getId(), consultation.getConsultationNo(), "USER",
                AuthContext.getUserId(), AuthContext.get().getUsername(),
                request.getConsultationContent(), null);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultation.getConsultationNo());
        result.put("reportNo", consultation.getReportNo());
        result.put("status", "PENDING");
        result.put("doctorId", consultation.getDoctorId());
        result.put("doctorName", consultation.getDoctorName());
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

    public List<DoctorConsultationEntity> doctorTodo() {
        return doctorConsultationMapper.selectList(new LambdaQueryWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getDoctorId, AuthContext.getUserId())
                .eq(DoctorConsultationEntity::getIsDeleted, 0)
                .in(DoctorConsultationEntity::getConsultationStatus, 0, 1)
                .orderByDesc(DoctorConsultationEntity::getPriorityLevel)
                .orderByAsc(DoctorConsultationEntity::getId));
    }

    public Map<String, Object> doctorDetail(String consultationNo) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getUserId().equals(consultation.getDoctorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return buildDetail(consultation);
    }

    @Transactional
    public Map<String, Object> reply(String consultationNo, ReplyConsultationRequest request) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (!AuthContext.getUserId().equals(consultation.getDoctorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (consultation.getConsultationStatus() != null && consultation.getConsultationStatus() == 3) {
            throw new BizException(ErrorCode.CONSULTATION_STATUS_INVALID);
        }

        insertReply(consultation.getId(), consultation.getConsultationNo(), "DOCTOR",
                AuthContext.getUserId(), AuthContext.get().getUsername(),
                request.getReplyContent(), request.getAttachmentUrl());

        LocalDateTime now = LocalDateTime.now();
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getConsultationStatus, 2)
                .set(DoctorConsultationEntity::getLatestReplyTime, now)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getUserId()));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultationNo);
        result.put("status", "REPLIED");
        result.put("replyTime", now.toString());
        return result;
    }

    @Transactional
    public Map<String, Object> assign(String consultationNo, AssignConsultationRequest request) {
        DoctorConsultationEntity consultation = getByNo(consultationNo);
        if (consultation.getConsultationStatus() != null && consultation.getConsultationStatus() == 3) {
            throw new BizException(ErrorCode.CONSULTATION_STATUS_INVALID);
        }
        String doctorName = resolveUserName(request.getDoctorId(), DEFAULT_DOCTOR_NAME);
        doctorConsultationMapper.update(null, new LambdaUpdateWrapper<DoctorConsultationEntity>()
                .eq(DoctorConsultationEntity::getId, consultation.getId())
                .set(DoctorConsultationEntity::getDoctorId, request.getDoctorId())
                .set(DoctorConsultationEntity::getDoctorName, doctorName)
                .set(DoctorConsultationEntity::getConsultationStatus, 1)
                .set(DoctorConsultationEntity::getUpdatedBy, AuthContext.getUserId()));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("consultationNo", consultationNo);
        result.put("doctorId", request.getDoctorId());
        result.put("doctorName", doctorName);
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
                        .eq(DoctorConsultationEntity::getIsDeleted, 0));
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
                             String attachmentUrl) {
        DoctorConsultationReplyEntity reply = new DoctorConsultationReplyEntity();
        reply.setConsultationId(consultationId);
        reply.setConsultationNo(consultationNo);
        reply.setReplyRole(role);
        reply.setReplyUserId(replyUserId);
        reply.setReplyUserName(replyUserName);
        reply.setReplyContent(replyContent);
        reply.setAttachmentUrl(attachmentUrl);
        reply.setReplyTime(LocalDateTime.now());
        reply.setCreatedBy(replyUserId);
        reply.setUpdatedBy(replyUserId);
        reply.setIsDeleted(0);
        doctorConsultationReplyMapper.insert(reply);
    }

    private String resolveUserName(Long userId, String defaultName) {
        if (userId == null) {
            return defaultName;
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getName() == null || user.getName().trim().isEmpty()) {
            return defaultName;
        }
        return user.getName();
    }
}
