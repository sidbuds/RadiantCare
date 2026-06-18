package com.xixin.health.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.exam.entity.ExamResultEntity;
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.mapper.ExamResultMapper;
import com.xixin.health.exam.service.ExamService;
import com.xixin.health.report.dto.GenerateReportRequest;
import com.xixin.health.report.dto.ReviewReportRequest;
import com.xixin.health.report.entity.DoctorReviewRecordEntity;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.entity.ExamReportItemEntity;
import com.xixin.health.report.entity.ReportTemplateEntity;
import com.xixin.health.report.mapper.DoctorReviewRecordMapper;
import com.xixin.health.report.mapper.ExamReportItemMapper;
import com.xixin.health.report.mapper.ExamReportMapper;
import com.xixin.health.report.mapper.ReportTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ExamService examService;
    private final ExamResultMapper examResultMapper;
    private final ReportTemplateMapper reportTemplateMapper;
    private final ExamReportMapper examReportMapper;
    private final ExamReportItemMapper examReportItemMapper;
    private final DoctorReviewRecordMapper doctorReviewRecordMapper;

    public ReportService(ExamService examService,
                         ExamResultMapper examResultMapper,
                         ReportTemplateMapper reportTemplateMapper,
                         ExamReportMapper examReportMapper,
                         ExamReportItemMapper examReportItemMapper,
                         DoctorReviewRecordMapper doctorReviewRecordMapper) {
        this.examService = examService;
        this.examResultMapper = examResultMapper;
        this.reportTemplateMapper = reportTemplateMapper;
        this.examReportMapper = examReportMapper;
        this.examReportItemMapper = examReportItemMapper;
        this.doctorReviewRecordMapper = doctorReviewRecordMapper;
    }

    @Transactional
    public Map<String, Object> generate(GenerateReportRequest request) {
        ExamTaskEntity task = examService.getTaskByNo(request.getTaskNo());
        if (task.getTaskStatus() == null || task.getTaskStatus() != 2) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "体检任务未完成，不能生成报告");
        }
        List<ExamResultEntity> results = examResultMapper.selectList(new LambdaQueryWrapper<ExamResultEntity>()
                .eq(ExamResultEntity::getTaskId, task.getId())
                .eq(ExamResultEntity::getIsDeleted, 0)
                .orderByAsc(ExamResultEntity::getId));
        if (results.isEmpty()) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "尚无检查结果，不能生成报告");
        }
        ReportTemplateEntity template = reportTemplateMapper.selectOne(new LambdaQueryWrapper<ReportTemplateEntity>()
                .eq(request.getTemplateCode() != null, ReportTemplateEntity::getTemplateCode, request.getTemplateCode())
                .eq(ReportTemplateEntity::getPackageId, task.getPackageId())
                .eq(ReportTemplateEntity::getStatus, 1)
                .eq(ReportTemplateEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (template == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "未配置可用报告模板");
        }
        ExamReportEntity existed = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getTaskId, task.getId())
                .eq(ExamReportEntity::getIsDeleted, 0));
        if (existed != null) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该任务已生成报告");
        }

        ExamReportEntity report = new ExamReportEntity();
        report.setReportNo(NoGenerator.next("RPT"));
        report.setAppointmentId(task.getAppointmentId());
        report.setTaskId(task.getId());
        report.setUserId(task.getUserId());
        report.setPackageId(task.getPackageId());
        report.setTemplateId(template.getId());
        report.setReportDate(LocalDate.now());
        report.setOverallConclusion(buildConclusion(results));
        report.setStatus(1);
        report.setVersionNo(1);
        report.setIsDeleted(0);
        examReportMapper.insert(report);

        int sort = 1;
        for (ExamResultEntity result : results) {
            ExamReportItemEntity item = new ExamReportItemEntity();
            item.setReportId(report.getId());
            item.setItemCode(result.getItemCode());
            item.setItemName(result.getItemName());
            item.setResultValue(result.getResultValue());
            item.setResultNumber(result.getResultNumber());
            item.setUnit(result.getUnit());
            item.setRefRange(result.getRefRange());
            item.setIsAbnormal(result.getIsAbnormal());
            item.setAbnormalLevel(result.getAbnormalLevel());
            item.setSortNo(sort++);
            item.setIsDeleted(0);
            examReportItemMapper.insert(item);
            result.setReportId(report.getId());
            examResultMapper.updateById(result);
        }

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("reportNo", report.getReportNo());
        response.put("status", "GENERATED");
        response.put("templateCode", template.getTemplateCode());
        response.put("sectionCount", results.size());
        return response;
    }

    @Transactional
    public Map<String, Object> review(String reportNo, ReviewReportRequest request) {
        ExamReportEntity report = getByNo(reportNo);
        DoctorReviewRecordEntity review = new DoctorReviewRecordEntity();
        review.setReviewNo(NoGenerator.next("RVW"));
        review.setReportId(report.getId());
        review.setTaskId(report.getTaskId());
        review.setReviewStage(request.getReviewStage());
        review.setReviewStatus(request.getReviewStatus());
        review.setReviewComment(request.getReviewComment());
        review.setReviewerId(AuthContext.getUserId());
        review.setReviewerName(AuthContext.get().getUsername());
        review.setReviewedAt(LocalDateTime.now());
        review.setIsDeleted(0);
        doctorReviewRecordMapper.insert(review);
        if ("PASS".equalsIgnoreCase(request.getReviewStatus())) {
            examReportMapper.update(null, new LambdaUpdateWrapper<ExamReportEntity>()
                    .eq(ExamReportEntity::getId, report.getId())
                    .set(ExamReportEntity::getStatus, 2));
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("reportNo", reportNo);
        result.put("reviewStatus", request.getReviewStatus());
        result.put("nextAction", "PASS".equalsIgnoreCase(request.getReviewStatus()) ? "CAN_PUBLISH" : "REJECTED");
        return result;
    }

    @Transactional
    public Map<String, Object> publish(String reportNo) {
        ExamReportEntity report = getByNo(reportNo);
        if (report.getStatus() == null || report.getStatus() != 2) {
            throw new BizException(ErrorCode.REPORT_REVIEW_REQUIRED);
        }
        LocalDateTime now = LocalDateTime.now();
        examReportMapper.update(null, new LambdaUpdateWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getId, report.getId())
                .set(ExamReportEntity::getStatus, 3)
                .set(ExamReportEntity::getPublishedAt, now));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("reportNo", reportNo);
        result.put("status", "PUBLISHED");
        result.put("publishedAt", now.toString());
        return result;
    }

    public List<ExamReportEntity> mine() {
        return examReportMapper.selectList(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getUserId, AuthContext.getUserId())
                .eq(ExamReportEntity::getStatus, 3)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .orderByDesc(ExamReportEntity::getId));
    }

    public ExamReportEntity detail(String reportNo) {
        ExamReportEntity report = getByNo(reportNo);
        if (!report.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (report.getStatus() == null || report.getStatus() != 3) {
            throw new BizException(5001, "报告未发布");
        }
        return report;
    }

    public List<ExamReportItemEntity> items(String reportNo) {
        ExamReportEntity report = detail(reportNo);
        return examReportItemMapper.selectList(new LambdaQueryWrapper<ExamReportItemEntity>()
                .eq(ExamReportItemEntity::getReportId, report.getId())
                .eq(ExamReportItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamReportItemEntity::getSortNo));
    }

    public ExamReportEntity getByNo(String reportNo) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, reportNo)
                .eq(ExamReportEntity::getIsDeleted, 0));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在");
        }
        return report;
    }

    private String buildConclusion(List<ExamResultEntity> results) {
        StringBuilder builder = new StringBuilder();
        for (ExamResultEntity result : results) {
            if (result.getConclusion() != null && result.getConclusion().trim().length() > 0) {
                if (builder.length() > 0) {
                    builder.append("；");
                }
                builder.append(result.getConclusion());
            }
        }
        return builder.length() == 0 ? "本次体检结果已生成，请结合医生建议查看指标详情。" : builder.toString();
    }
}
