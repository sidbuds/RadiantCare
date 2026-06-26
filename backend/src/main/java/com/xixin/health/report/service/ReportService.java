package com.xixin.health.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.exam.entity.ExamResultEntity;
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.mapper.ExamResultMapper;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.mapper.OrderMapper;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 报告服务 - 处理报告生成/审核/发布
 */
@Service
public class ReportService {

    private static final int REPORT_WAIT_REVIEW = 1;
    private static final int REPORT_PUBLISHED = 3;
    private static final int REPORT_ADMIN_REQUIRED = 4;

    private static final String REVIEW_PENDING = "PENDING";
    private static final String REVIEW_PASS = "PASS";
    private static final String REVIEW_REJECT = "REJECT";

    private final ExamTaskMapper examTaskMapper;
    private final ExamResultMapper examResultMapper;
    private final ReportTemplateMapper reportTemplateMapper;
    private final ExamReportMapper examReportMapper;
    private final ExamReportItemMapper examReportItemMapper;
    private final DoctorReviewRecordMapper doctorReviewRecordMapper;
    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final AppointmentMapper appointmentMapper;
    private final OrderMapper orderMapper;

    public ReportService(ExamTaskMapper examTaskMapper,
                         ExamResultMapper examResultMapper,
                         ReportTemplateMapper reportTemplateMapper,
                         ExamReportMapper examReportMapper,
                         ExamReportItemMapper examReportItemMapper,
                         DoctorReviewRecordMapper doctorReviewRecordMapper,
                         StaffAccountMapper staffAccountMapper,
                         StaffRoleRelMapper staffRoleRelMapper,
                         AppointmentMapper appointmentMapper,
                         OrderMapper orderMapper) {
        this.examTaskMapper = examTaskMapper;
        this.examResultMapper = examResultMapper;
        this.reportTemplateMapper = reportTemplateMapper;
        this.examReportMapper = examReportMapper;
        this.examReportItemMapper = examReportItemMapper;
        this.doctorReviewRecordMapper = doctorReviewRecordMapper;
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.appointmentMapper = appointmentMapper;
        this.orderMapper = orderMapper;
    }

    /** 生成报告 */
    @Transactional
    public Map<String, Object> generate(GenerateReportRequest request) {
        ExamTaskEntity task = getTaskByNo(request.getTaskNo());
        return generateForCompletedTask(task, request.getTemplateCode(), false);
    }

    /** 自动为完成的任务生成报告 */
    @Transactional
    public Map<String, Object> autoGenerateForCompletedTask(String taskNo) {
        ExamTaskEntity task = getTaskByNo(taskNo);
        return generateForCompletedTask(task, null, true);
    }

    private Map<String, Object> generateForCompletedTask(ExamTaskEntity task, String templateCode, boolean autoAssignReviewers) {
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
                .eq(templateCode != null && templateCode.trim().length() > 0, ReportTemplateEntity::getTemplateCode, templateCode)
                .eq(ReportTemplateEntity::getPackageId, task.getPackageId())
                .eq(ReportTemplateEntity::getStatus, 1)
                .eq(ReportTemplateEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (template == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "未配置可用报告模板");
        }

        ExamReportEntity existed = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getTaskId, task.getId())
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (existed != null) {
            return buildGenerateResponse(existed, template.getTemplateCode(), results.size(), true, 0);
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
        report.setStatus(REPORT_WAIT_REVIEW);
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

        int reviewerCount = 0;
        if (autoAssignReviewers) {
            reviewerCount = assignReviewers(report, results);
            if (reviewerCount < 2) {
                markReportAdminRequired(report.getId());
            }
        }
        return buildGenerateResponse(report, template.getTemplateCode(), results.size(), false, reviewerCount);
    }

    /** 审核报告 */
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
        review.setReviewerId(AuthContext.getAccountId());
        review.setReviewerName(AuthContext.get().getUsername());
        review.setReviewedAt(LocalDateTime.now());
        review.setIsDeleted(0);
        doctorReviewRecordMapper.insert(review);
        return buildReviewResult(reportNo, request.getReviewStatus(), "MANUAL");
    }

    /** 发布报告 */
    @Transactional
    public Map<String, Object> publish(String reportNo) {
        ExamReportEntity report = getByNo(reportNo);
        if (report.getStatus() == null || report.getStatus() == REPORT_PUBLISHED) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("reportNo", reportNo);
            result.put("status", "PUBLISHED");
            result.put("publishedAt", report.getPublishedAt() == null ? null : report.getPublishedAt().toString());
            return result;
        }
        if (report.getStatus() != REPORT_WAIT_REVIEW) {
            throw new BizException(ErrorCode.REPORT_REVIEW_REQUIRED);
        }
        publishAndFinalize(report);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("reportNo", reportNo);
        result.put("status", "PUBLISHED");
        result.put("publishedAt", LocalDateTime.now().toString());
        return result;
    }

    /** 查询当前用户报告列表 */
    public List<ExamReportEntity> mine() {
        return examReportMapper.selectList(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getUserId, AuthContext.getUserId())
                .eq(ExamReportEntity::getStatus, REPORT_PUBLISHED)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .orderByDesc(ExamReportEntity::getPublishedAt)
                .orderByDesc(ExamReportEntity::getId));
    }

    /** 查询报告详情 */
    public ExamReportEntity detail(String reportNo) {
        ExamReportEntity report = getByNo(reportNo);
        if (!report.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (report.getStatus() == null || report.getStatus() != REPORT_PUBLISHED) {
            throw new BizException(5001, "报告未发布");
        }
        return report;
    }

    /** 查询报告检查项 */
    public List<ExamReportItemEntity> items(String reportNo) {
        ExamReportEntity report = detail(reportNo);
        return examReportItemMapper.selectList(new LambdaQueryWrapper<ExamReportItemEntity>()
                .eq(ExamReportItemEntity::getReportId, report.getId())
                .eq(ExamReportItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamReportItemEntity::getSortNo));
    }

    /** 查询医生待审核报告 */
    public List<Map<String, Object>> doctorReviewTodo() {
        return doctorReviewsByStatus(REVIEW_PENDING);
    }

    /** 查询医生审核历史 */
    public List<Map<String, Object>> doctorReviewHistory() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<DoctorReviewRecordEntity> reviews = doctorReviewRecordMapper.selectList(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getReviewerId, AuthContext.getAccountId())
                .ne(DoctorReviewRecordEntity::getReviewStatus, REVIEW_PENDING)
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0)
                .orderByDesc(DoctorReviewRecordEntity::getReviewedAt)
                .orderByDesc(DoctorReviewRecordEntity::getId));
        for (DoctorReviewRecordEntity review : reviews) {
            ExamReportEntity report = examReportMapper.selectById(review.getReportId());
            if (report == null) {
                continue;
            }
            Map<String, Object> row = buildDoctorReviewRow(report, review);
            row.put("reviewComment", review.getReviewComment());
            row.put("reviewedAt", review.getReviewedAt());
            result.add(row);
        }
        return result;
    }

    /** 查询医生审核详情 */
    public Map<String, Object> doctorReviewDetail(String reportNo) {
        ExamReportEntity report = getByNo(reportNo);
        DoctorReviewRecordEntity review = getPendingReviewForDoctor(report.getId());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("report", report);
        result.put("review", review);
        result.put("items", examReportItemMapper.selectList(new LambdaQueryWrapper<ExamReportItemEntity>()
                .eq(ExamReportItemEntity::getReportId, report.getId())
                .eq(ExamReportItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamReportItemEntity::getSortNo)));
        return result;
    }

    /** 医生审核报告 */
    @Transactional
    public Map<String, Object> doctorReview(String reportNo, ReviewReportRequest request) {
        ExamReportEntity report = getByNo(reportNo);
        DoctorReviewRecordEntity review = getPendingReviewForDoctor(report.getId());

        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getAppointmentId, report.getAppointmentId())
                .eq(OrderEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (order != null && (Integer.valueOf(2).equals(order.getStatus()) || Integer.valueOf(3).equals(order.getStatus()))) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "订单已退款或退款中，不能继续审核");
        }

        String reviewStatus = request.getReviewStatus() == null ? "" : request.getReviewStatus().trim().toUpperCase();
        if (!REVIEW_PASS.equals(reviewStatus) && !REVIEW_REJECT.equals(reviewStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "审核结果只能为 PASS 或 REJECT");
        }

        int updated = doctorReviewRecordMapper.update(null, new LambdaUpdateWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getId, review.getId())
                .eq(DoctorReviewRecordEntity::getReviewStatus, REVIEW_PENDING)
                .set(DoctorReviewRecordEntity::getReviewStage, request.getReviewStage())
                .set(DoctorReviewRecordEntity::getReviewStatus, reviewStatus)
                .set(DoctorReviewRecordEntity::getReviewComment, request.getReviewComment())
                .set(DoctorReviewRecordEntity::getReviewedAt, LocalDateTime.now()));
        if (updated == 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该审核记录已处理，不能重复提交");
        }

        if (REVIEW_REJECT.equals(reviewStatus)) {
            markReportAdminRequired(report.getId());
            return buildReviewResult(reportNo, REVIEW_REJECT, "ADMIN_REQUIRED");
        }

        long pendingCount = doctorReviewRecordMapper.selectCount(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getReportId, report.getId())
                .eq(DoctorReviewRecordEntity::getReviewStatus, REVIEW_PENDING)
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0));
        long passCount = doctorReviewRecordMapper.selectCount(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getReportId, report.getId())
                .eq(DoctorReviewRecordEntity::getReviewStatus, REVIEW_PASS)
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0));
        if (pendingCount == 0 && passCount >= 2) {
            publishAndFinalize(report);
            return buildReviewResult(reportNo, REVIEW_PASS, "PUBLISHED");
        }
        return buildReviewResult(reportNo, REVIEW_PASS, "WAIT_OTHER_REVIEWER");
    }

    public ExamReportEntity getByNo(String reportNo) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, reportNo)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在");
        }
        return report;
    }

    private List<Map<String, Object>> doctorReviewsByStatus(String status) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<DoctorReviewRecordEntity> reviews = doctorReviewRecordMapper.selectList(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getReviewerId, AuthContext.getAccountId())
                .eq(DoctorReviewRecordEntity::getReviewStatus, status)
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0)
                .orderByDesc(DoctorReviewRecordEntity::getId));
        for (DoctorReviewRecordEntity review : reviews) {
            ExamReportEntity report = examReportMapper.selectById(review.getReportId());
            if (report == null) {
                continue;
            }
            result.add(buildDoctorReviewRow(report, review));
        }
        return result;
    }

    private Map<String, Object> buildDoctorReviewRow(ExamReportEntity report, DoctorReviewRecordEntity review) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("reviewNo", review.getReviewNo());
        row.put("reportNo", report.getReportNo());
        row.put("taskId", report.getTaskId());
        row.put("packageId", report.getPackageId());
        row.put("reportDate", report.getReportDate());
        row.put("status", report.getStatus());
        row.put("overallConclusion", report.getOverallConclusion());
        row.put("reviewStatus", review.getReviewStatus());
        return row;
    }

    private Map<String, Object> buildGenerateResponse(ExamReportEntity report,
                                                      String templateCode,
                                                      int sectionCount,
                                                      boolean isExisting,
                                                      int reviewerCount) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("reportNo", report.getReportNo());
        response.put("status", getReportStatusStr(report.getStatus()));
        response.put("templateCode", templateCode);
        response.put("sectionCount", sectionCount);
        response.put("isExisting", isExisting);
        if (reviewerCount > 0) {
            response.put("reviewerCount", reviewerCount);
        }
        return response;
    }

    private Map<String, Object> buildReviewResult(String reportNo, String reviewStatus, String nextAction) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("reportNo", reportNo);
        result.put("reviewStatus", reviewStatus);
        result.put("nextAction", nextAction);
        return result;
    }

    private int assignReviewers(ExamReportEntity report, List<ExamResultEntity> results) {
        long existingCount = doctorReviewRecordMapper.selectCount(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getReportId, report.getId())
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0));
        if (existingCount > 0) {
            return (int) existingCount;
        }

        Set<Long> reviewerIds = new LinkedHashSet<Long>();
        List<Long> entryDoctorIds = new ArrayList<Long>();
        for (ExamResultEntity result : results) {
            if (result.getEntryDoctorId() != null && !entryDoctorIds.contains(result.getEntryDoctorId())) {
                entryDoctorIds.add(result.getEntryDoctorId());
            }
        }
        Collections.shuffle(entryDoctorIds);
        reviewerIds.addAll(entryDoctorIds);

        List<StaffAccountEntity> doctors = staffAccountMapper.selectList(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getStatus, 1)
                .eq(StaffAccountEntity::getIsDeleted, 0));
        Collections.shuffle(doctors);
        if (reviewerIds.size() < 2) {
            for (StaffAccountEntity doctor : doctors) {
                if (reviewerIds.size() >= 2) {
                    break;
                }
                if (isDoctorAccount(doctor.getId())) {
                    reviewerIds.add(doctor.getId());
                }
            }
        }

        int sort = 1;
        for (Long reviewerId : reviewerIds) {
            if (sort > 2) {
                break;
            }
            StaffAccountEntity doctor = staffAccountMapper.selectById(reviewerId);
            DoctorReviewRecordEntity review = new DoctorReviewRecordEntity();
            review.setReviewNo(NoGenerator.next("RVW"));
            review.setReportId(report.getId());
            review.setTaskId(report.getTaskId());
            review.setReviewStage("AUTO_" + sort);
            review.setReviewStatus(REVIEW_PENDING);
            review.setReviewComment("AUTO_ASSIGNED");
            review.setReviewerId(reviewerId);
            review.setReviewerName(doctor == null ? null : doctor.getDisplayName());
            review.setIsDeleted(0);
            doctorReviewRecordMapper.insert(review);
            sort++;
        }
        return sort - 1;
    }

    private boolean isDoctorAccount(Long accountId) {
        Long count = staffRoleRelMapper.selectCount(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, accountId)
                .eq(StaffRoleRelEntity::getRoleCode, "DOCTOR")
                .eq(StaffRoleRelEntity::getIsDeleted, 0));
        return count != null && count > 0;
    }

    private DoctorReviewRecordEntity getPendingReviewForDoctor(Long reportId) {
        DoctorReviewRecordEntity review = doctorReviewRecordMapper.selectOne(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .eq(DoctorReviewRecordEntity::getReportId, reportId)
                .eq(DoctorReviewRecordEntity::getReviewerId, AuthContext.getAccountId())
                .eq(DoctorReviewRecordEntity::getReviewStatus, REVIEW_PENDING)
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (review == null) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "当前医生没有该报告的待审核任务");
        }
        return review;
    }

    private ExamTaskEntity getTaskByNo(String taskNo) {
        ExamTaskEntity task = examTaskMapper.selectOne(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getTaskNo, taskNo)
                .eq(ExamTaskEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (task == null) {
            throw new BizException(ErrorCode.EXAM_TASK_NOT_FOUND);
        }
        return task;
    }

    private void publishAndFinalize(ExamReportEntity report) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getAppointmentId, report.getAppointmentId())
                .eq(OrderEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (order != null && (Integer.valueOf(2).equals(order.getStatus()) || Integer.valueOf(3).equals(order.getStatus()))) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "订单已退款或退款中，不能发布报告");
        }

        LocalDateTime now = LocalDateTime.now();
        examReportMapper.update(null, new LambdaUpdateWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getId, report.getId())
                .set(ExamReportEntity::getStatus, REPORT_PUBLISHED)
                .set(ExamReportEntity::getPublishedAt, now));

        if (order != null) {
            orderMapper.update(null, new LambdaUpdateWrapper<OrderEntity>()
                    .eq(OrderEntity::getId, order.getId())
                    .eq(OrderEntity::getStatus, 1)
                    .set(OrderEntity::getStatus, 4));
        }

        appointmentMapper.update(null, new LambdaUpdateWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getId, report.getAppointmentId())
                .eq(AppointmentEntity::getStatus, 1)
                .set(AppointmentEntity::getStatus, 2));
    }

    private void markReportAdminRequired(Long reportId) {
        examReportMapper.update(null, new LambdaUpdateWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getId, reportId)
                .set(ExamReportEntity::getStatus, REPORT_ADMIN_REQUIRED));
    }

    private String getReportStatusStr(Integer status) {
        if (status == null) return "UNKNOWN";
        switch (status) {
            case REPORT_WAIT_REVIEW:
                return "WAIT_REVIEW";
            case REPORT_PUBLISHED:
                return "PUBLISHED";
            case REPORT_ADMIN_REQUIRED:
                return "ADMIN_REQUIRED";
            default:
                return "UNKNOWN";
        }
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
