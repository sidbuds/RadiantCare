package com.xixin.health.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.ai.dto.ReportAgentChatRequest;
import com.xixin.health.ai.dto.ReportAgentSendReportRequest;
import com.xixin.health.ai.entity.AiAgentSessionEntity;
import com.xixin.health.ai.entity.AiAgentSessionReportEntity;
import com.xixin.health.ai.mapper.AiAgentSessionMapper;
import com.xixin.health.ai.mapper.AiAgentSessionReportMapper;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.SystemConfigService;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.entity.ExamReportItemEntity;
import com.xixin.health.report.mapper.ExamReportItemMapper;
import com.xixin.health.report.mapper.ExamReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportAgentService {

    private static final String SAFETY_NOTICE = "AI建议不能替代医生诊断，如有不适请及时咨询医生。";

    private final AiAgentSessionMapper sessionMapper;
    private final AiAgentSessionReportMapper sessionReportMapper;
    private final ExamReportMapper reportMapper;
    private final ExamReportItemMapper reportItemMapper;
    private final ExamPackageMapper packageMapper;
    private final ExamPackageItemMapper packageItemMapper;
    private final SystemConfigService systemConfigService;
    private final ReportAgentModelClient modelClient;

    public ReportAgentService(AiAgentSessionMapper sessionMapper,
                              AiAgentSessionReportMapper sessionReportMapper,
                              ExamReportMapper reportMapper,
                              ExamReportItemMapper reportItemMapper,
                              ExamPackageMapper packageMapper,
                              ExamPackageItemMapper packageItemMapper,
                              SystemConfigService systemConfigService,
                              ReportAgentModelClient modelClient) {
        this.sessionMapper = sessionMapper;
        this.sessionReportMapper = sessionReportMapper;
        this.reportMapper = reportMapper;
        this.reportItemMapper = reportItemMapper;
        this.packageMapper = packageMapper;
        this.packageItemMapper = packageItemMapper;
        this.systemConfigService = systemConfigService;
        this.modelClient = modelClient;
    }

    @Transactional
    public Map<String, Object> createSession() {
        Long userId = requireUserId();
        AiAgentSessionEntity entity = new AiAgentSessionEntity();
        entity.setSessionNo(NoGenerator.next("AIS"));
        entity.setUserId(userId);
        entity.setStatus(1);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setIsDeleted(0);
        sessionMapper.insert(entity);
        return buildSession(entity, Collections.<AiAgentSessionReportEntity>emptyList());
    }

    public Map<String, Object> currentSession() {
        Long userId = requireUserId();
        AiAgentSessionEntity session = sessionMapper.selectOne(new LambdaQueryWrapper<AiAgentSessionEntity>()
                .eq(AiAgentSessionEntity::getUserId, userId)
                .eq(AiAgentSessionEntity::getIsDeleted, 0)
                .orderByDesc(AiAgentSessionEntity::getId)
                .last("limit 1"));
        if (session == null) {
            return createSession();
        }
        return buildSession(session, sessionReports(session.getId()));
    }

    @Transactional
    public Map<String, Object> sendReport(String sessionNo, ReportAgentSendReportRequest request) {
        AiAgentSessionEntity session = requireSession(sessionNo);
        ExamReportEntity report = reportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, request.getReportNo())
                .eq(ExamReportEntity::getUserId, requireUserId())
                .eq(ExamReportEntity::getStatus, 3)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在或未发布");
        }
        Long count = sessionReportMapper.selectCount(new LambdaQueryWrapper<AiAgentSessionReportEntity>()
                .eq(AiAgentSessionReportEntity::getSessionId, session.getId())
                .eq(AiAgentSessionReportEntity::getReportId, report.getId())
                .eq(AiAgentSessionReportEntity::getIsDeleted, 0));
        if (count == null || count == 0) {
            AiAgentSessionReportEntity rel = new AiAgentSessionReportEntity();
            rel.setSessionId(session.getId());
            rel.setSessionNo(session.getSessionNo());
            rel.setReportId(report.getId());
            rel.setReportNo(report.getReportNo());
            rel.setCreatedBy(requireUserId());
            rel.setUpdatedBy(requireUserId());
            rel.setIsDeleted(0);
            sessionReportMapper.insert(rel);
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("sessionNo", session.getSessionNo());
        result.put("reportNo", report.getReportNo());
        result.put("status", "AUTHORIZED");
        return result;
    }

    @Transactional
    public void revokeReport(String sessionNo, String reportNo) {
        AiAgentSessionEntity session = requireSession(sessionNo);
        Long userId = requireUserId();
        AiAgentSessionReportEntity rel = sessionReportMapper.selectOne(new LambdaQueryWrapper<AiAgentSessionReportEntity>()
                .eq(AiAgentSessionReportEntity::getSessionId, session.getId())
                .eq(AiAgentSessionReportEntity::getReportNo, reportNo)
                .eq(AiAgentSessionReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (rel == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "未找到该报告的授权记录");
        }
        rel.setIsDeleted(1);
        rel.setUpdatedBy(userId);
        rel.setUpdatedAt(LocalDateTime.now());
        sessionReportMapper.updateById(rel);
    }

    public Map<String, Object> chat(String sessionNo, ReportAgentChatRequest request) {
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "问题不能为空");
        }
        AiAgentSessionEntity session = requireSession(sessionNo);
        List<AiAgentSessionReportEntity> authorized = sessionReports(session.getId());
        if (authorized.isEmpty() && asksReportAnalysis(request.getQuestion())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "请先发送报告给智能助手，再进行报告分析");
        }
        ReportAgentPrompt prompt = new ReportAgentPrompt();
        prompt.setQuestion(request.getQuestion().trim());
        prompt.setSystemPrompt(systemConfigService.getValue("ai.agent.system_prompt", ""));
        prompt.setReports(buildReports(authorized));
        prompt.setPackages(buildPackageCandidates());
        String raw = modelClient.chat(prompt);
        return parseModelResult(raw, prompt.getPackages());
    }

    private AiAgentSessionEntity requireSession(String sessionNo) {
        AiAgentSessionEntity session = sessionMapper.selectOne(new LambdaQueryWrapper<AiAgentSessionEntity>()
                .eq(AiAgentSessionEntity::getSessionNo, sessionNo)
                .eq(AiAgentSessionEntity::getUserId, requireUserId())
                .eq(AiAgentSessionEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (session == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "AI会话不存在");
        }
        return session;
    }

    private List<AiAgentSessionReportEntity> sessionReports(Long sessionId) {
        return sessionReportMapper.selectList(new LambdaQueryWrapper<AiAgentSessionReportEntity>()
                .eq(AiAgentSessionReportEntity::getSessionId, sessionId)
                .eq(AiAgentSessionReportEntity::getIsDeleted, 0)
                .orderByAsc(AiAgentSessionReportEntity::getId));
    }

    private List<Map<String, Object>> buildReports(List<AiAgentSessionReportEntity> authorized) {
        List<Map<String, Object>> reports = new ArrayList<Map<String, Object>>();
        for (AiAgentSessionReportEntity rel : authorized) {
            ExamReportEntity report = reportMapper.selectById(rel.getReportId());
            if (report == null || !requireUserId().equals(report.getUserId()) || !Integer.valueOf(3).equals(report.getStatus())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("reportNo", report.getReportNo());
            row.put("overallConclusion", report.getOverallConclusion());
            List<ExamReportItemEntity> items = reportItemMapper.selectList(new LambdaQueryWrapper<ExamReportItemEntity>()
                    .eq(ExamReportItemEntity::getReportId, report.getId())
                    .eq(ExamReportItemEntity::getIsDeleted, 0)
                    .orderByAsc(ExamReportItemEntity::getSortNo));
            row.put("items", items);
            reports.add(row);
        }
        return reports;
    }

    private List<Map<String, Object>> buildPackageCandidates() {
        List<ExamPackageEntity> packages = packageMapper.selectList(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getStatus, 1)
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageEntity::getId));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExamPackageEntity pkg : packages) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("packageId", pkg.getId());
            row.put("packageCode", pkg.getPackageCode());
            row.put("packageName", pkg.getPackageName());
            row.put("category", pkg.getCategory());
            row.put("price", pkg.getPrice());
            row.put("remark", pkg.getRemark());
            row.put("actionUrl", "/user/appointments/create?packageId=" + pkg.getId() + "&packageCode=" + pkg.getPackageCode());
            row.put("items", packageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                    .eq(ExamPackageItemEntity::getPackageId, pkg.getId())
                    .eq(ExamPackageItemEntity::getIsDeleted, 0)
                    .orderByAsc(ExamPackageItemEntity::getSortNo)));
            result.add(row);
            if (result.size() >= 20) {
                break;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseModelResult(String raw, List<Map<String, Object>> packages) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> result = mapper.readValue(extractJsonObject(raw), Map.class);
            if (!result.containsKey("safetyNotice") || result.get("safetyNotice") == null) {
                result.put("safetyNotice", SAFETY_NOTICE);
            }
            completeRecommendedPackages(result, packages);
            return result;
        } catch (Exception ex) {
            Map<String, Object> fallback = new LinkedHashMap<String, Object>();
            fallback.put("answer", raw);
            fallback.put("orderPitch", "");
            fallback.put("recommendedPackages", Collections.emptyList());
            fallback.put("safetyNotice", SAFETY_NOTICE);
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private void completeRecommendedPackages(Map<String, Object> result, List<Map<String, Object>> packages) {
        Object value = result.get("recommendedPackages");
        if (!(value instanceof List)) {
            return;
        }
        Map<String, Map<String, Object>> packageById = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> pkg : packages) {
            Object packageId = pkg.get("packageId");
            if (packageId != null) {
                packageById.put(String.valueOf(packageId), pkg);
            }
        }
        for (Object item : (List<Object>) value) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<String, Object> row = (Map<String, Object>) item;
            Map<String, Object> source = packageById.get(String.valueOf(row.get("packageId")));
            if (source == null) {
                continue;
            }
            copyIfMissing(row, source, "packageCode");
            copyIfMissing(row, source, "packageName");
            copyIfMissing(row, source, "category");
            copyIfMissing(row, source, "price");
            copyIfMissing(row, source, "actionUrl");
        }
    }

    private void copyIfMissing(Map<String, Object> target, Map<String, Object> source, String key) {
        Object value = target.get(key);
        if ((value == null || String.valueOf(value).trim().isEmpty()) && source.get(key) != null) {
            target.put(key, source.get(key));
        }
    }

    private String extractJsonObject(String raw) {
        if (raw == null) {
            return "{}";
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd >= 0) {
                text = text.substring(firstLineEnd + 1).trim();
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3).trim();
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private Map<String, Object> buildSession(AiAgentSessionEntity session, List<AiAgentSessionReportEntity> reports) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("sessionNo", session.getSessionNo());
        result.put("status", session.getStatus());
        result.put("reports", reports);
        return result;
    }

    private boolean asksReportAnalysis(String question) {
        String text = question == null ? "" : question.toLowerCase();
        return text.contains("报告") || text.contains("指标") || text.contains("异常") || text.contains("复查")
                || text.contains("report") || text.contains("血") || text.contains("尿") || text.contains("肝") || text.contains("肾");
    }

    private Long requireUserId() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
