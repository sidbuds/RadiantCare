package com.xixin.health.compare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.compare.dto.CreateCompareTaskRequest;
import com.xixin.health.compare.entity.HealthAdviceRecordEntity;
import com.xixin.health.compare.entity.HealthRiskScoreEntity;
import com.xixin.health.compare.entity.IndicatorTrendTagEntity;
import com.xixin.health.compare.entity.ReportCompareResultEntity;
import com.xixin.health.compare.entity.ReportCompareTaskEntity;
import com.xixin.health.compare.mapper.HealthAdviceRecordMapper;
import com.xixin.health.compare.mapper.HealthRiskScoreMapper;
import com.xixin.health.compare.mapper.IndicatorTrendTagMapper;
import com.xixin.health.compare.mapper.ReportCompareResultMapper;
import com.xixin.health.compare.mapper.ReportCompareTaskMapper;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.entity.ExamReportItemEntity;
import com.xixin.health.report.mapper.ExamReportItemMapper;
import com.xixin.health.report.mapper.ExamReportMapper;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告对比服务 - 处理报告对比/趋势分析/健康建议
 */
@Service
public class CompareService {

    private static final BigDecimal ZERO = new BigDecimal("0");
    private static final BigDecimal OBVIOUS_CHANGE_RATE = new BigDecimal("0.20");

    private final ReportCompareTaskMapper reportCompareTaskMapper;
    private final ReportCompareResultMapper reportCompareResultMapper;
    private final IndicatorTrendTagMapper indicatorTrendTagMapper;
    private final HealthRiskScoreMapper healthRiskScoreMapper;
    private final HealthAdviceRecordMapper healthAdviceRecordMapper;
    private final ExamReportMapper examReportMapper;
    private final ExamReportItemMapper examReportItemMapper;
    private final UserMapper userMapper;

    public CompareService(ReportCompareTaskMapper reportCompareTaskMapper,
                          ReportCompareResultMapper reportCompareResultMapper,
                          IndicatorTrendTagMapper indicatorTrendTagMapper,
                          HealthRiskScoreMapper healthRiskScoreMapper,
                          HealthAdviceRecordMapper healthAdviceRecordMapper,
                          ExamReportMapper examReportMapper,
                          ExamReportItemMapper examReportItemMapper,
                          UserMapper userMapper) {
        this.reportCompareTaskMapper = reportCompareTaskMapper;
        this.reportCompareResultMapper = reportCompareResultMapper;
        this.indicatorTrendTagMapper = indicatorTrendTagMapper;
        this.healthRiskScoreMapper = healthRiskScoreMapper;
        this.healthAdviceRecordMapper = healthAdviceRecordMapper;
        this.examReportMapper = examReportMapper;
        this.examReportItemMapper = examReportItemMapper;
        this.userMapper = userMapper;
    }

    /** 创建对比任务 */
    @Transactional
    public Map<String, Object> createTask(CreateCompareTaskRequest request) {
        if (request.getBaselineReportNo().equals(request.getCompareReportNo())) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "两份报告不能相同");
        }

        ExamReportEntity baselineReport = getOwnedPublishedReport(request.getBaselineReportNo());
        ExamReportEntity compareReport = getOwnedPublishedReport(request.getCompareReportNo());
        if (!baselineReport.getUserId().equals(compareReport.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        ReportCompareTaskEntity existed = reportCompareTaskMapper.selectOne(
                new LambdaQueryWrapper<ReportCompareTaskEntity>()
                        .eq(ReportCompareTaskEntity::getUserId, AuthContext.getUserId())
                        .eq(ReportCompareTaskEntity::getBaselineReportId, baselineReport.getId())
                        .eq(ReportCompareTaskEntity::getCompareReportId, compareReport.getId())
                        .eq(ReportCompareTaskEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (existed != null) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该对比任务已存在");
        }

        ReportCompareTaskEntity task = new ReportCompareTaskEntity();
        task.setTaskNo(NoGenerator.next("CMP"));
        task.setUserId(AuthContext.getUserId());
        task.setBaselineReportId(baselineReport.getId());
        task.setCompareReportId(compareReport.getId());
        task.setStatus(0);
        task.setCreatedBy(AuthContext.getUserId());
        task.setUpdatedBy(AuthContext.getUserId());
        task.setIsDeleted(0);
        reportCompareTaskMapper.insert(task);

        reportCompareTaskMapper.update(null, new LambdaUpdateWrapper<ReportCompareTaskEntity>()
                .eq(ReportCompareTaskEntity::getId, task.getId())
                .set(ReportCompareTaskEntity::getStatus, 1)
                .set(ReportCompareTaskEntity::getUpdatedBy, AuthContext.getUserId()));

        try {
            ExecutionContext context = executeCompare(task, baselineReport, compareReport);
            reportCompareTaskMapper.update(null, new LambdaUpdateWrapper<ReportCompareTaskEntity>()
                    .eq(ReportCompareTaskEntity::getId, task.getId())
                    .set(ReportCompareTaskEntity::getStatus, 2)
                    .set(ReportCompareTaskEntity::getUpdatedBy, AuthContext.getUserId()));

            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("taskNo", task.getTaskNo());
            result.put("status", "COMPLETED");
            result.put("baselineReportNo", baselineReport.getReportNo());
            result.put("compareReportNo", compareReport.getReportNo());
            result.put("comparableItemCount", context.results.size());
            result.put("incomparableItemCount", context.incomparableItems.size());
            result.put("riskLevel", context.riskScore.getRiskLevel());
            result.put("riskScore", context.riskScore.getScoreTotal());
            result.put("adviceCount", context.advices.size());
            return result;
        } catch (RuntimeException ex) {
            reportCompareTaskMapper.update(null, new LambdaUpdateWrapper<ReportCompareTaskEntity>()
                    .eq(ReportCompareTaskEntity::getId, task.getId())
                    .set(ReportCompareTaskEntity::getStatus, 3)
                    .set(ReportCompareTaskEntity::getUpdatedBy, AuthContext.getUserId()));
            throw ex;
        }
    }

    /** 查询对比任务详情 */
    public Map<String, Object> taskDetail(String taskNo) {
        ReportCompareTaskEntity task = getOwnedTask(taskNo);
        ExamReportEntity baselineReport = examReportMapper.selectById(task.getBaselineReportId());
        ExamReportEntity compareReport = examReportMapper.selectById(task.getCompareReportId());
        HealthRiskScoreEntity riskScore = getRiskScore(task.getId());

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("task", task);
        result.put("baselineReport", buildReportSummary(baselineReport));
        result.put("compareReport", buildReportSummary(compareReport));
        result.put("riskScore", riskScore);
        return result;
    }

    /** 查询对比结果 */
    public Map<String, Object> taskResults(String taskNo) {
        ReportCompareTaskEntity task = getOwnedTask(taskNo);
        ExamReportEntity baselineReport = examReportMapper.selectById(task.getBaselineReportId());
        ExamReportEntity compareReport = examReportMapper.selectById(task.getCompareReportId());

        List<ReportCompareResultEntity> results = reportCompareResultMapper.selectList(
                new LambdaQueryWrapper<ReportCompareResultEntity>()
                        .eq(ReportCompareResultEntity::getTaskId, task.getId())
                        .eq(ReportCompareResultEntity::getIsDeleted, 0)
                        .orderByAsc(ReportCompareResultEntity::getId));
        List<IndicatorTrendTagEntity> trendTags = indicatorTrendTagMapper.selectList(
                new LambdaQueryWrapper<IndicatorTrendTagEntity>()
                        .eq(IndicatorTrendTagEntity::getTaskId, task.getId())
                        .eq(IndicatorTrendTagEntity::getIsDeleted, 0)
                        .orderByAsc(IndicatorTrendTagEntity::getId));

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("task", task);
        result.put("baselineReport", buildReportSummary(baselineReport));
        result.put("compareReport", buildReportSummary(compareReport));
        result.put("compareResults", results);
        result.put("trendTags", trendTags);
        result.put("healthAdvices", listAdvices(task.getId()));
        return result;
    }

    /** 导出对比结果 */
    public Map<String, Object> export(String taskNo) {
        ReportCompareTaskEntity task = getOwnedTask(taskNo);
        ExamReportEntity baselineReport = examReportMapper.selectById(task.getBaselineReportId());
        ExamReportEntity compareReport = examReportMapper.selectById(task.getCompareReportId());
        UserEntity user = userMapper.selectById(task.getUserId());

        List<ReportCompareResultEntity> results = reportCompareResultMapper.selectList(
                new LambdaQueryWrapper<ReportCompareResultEntity>()
                        .eq(ReportCompareResultEntity::getTaskId, task.getId())
                        .eq(ReportCompareResultEntity::getIsDeleted, 0)
                        .orderByAsc(ReportCompareResultEntity::getId));
        List<IndicatorTrendTagEntity> trendTags = indicatorTrendTagMapper.selectList(
                new LambdaQueryWrapper<IndicatorTrendTagEntity>()
                        .eq(IndicatorTrendTagEntity::getTaskId, task.getId())
                        .eq(IndicatorTrendTagEntity::getIsDeleted, 0)
                        .orderByAsc(IndicatorTrendTagEntity::getId));

        Map<String, Object> export = new LinkedHashMap<String, Object>();
        export.put("taskNo", task.getTaskNo());
        export.put("status", task.getStatus());
        export.put("user", buildUserSummary(user));
        export.put("baselineReport", buildReportSummary(baselineReport));
        export.put("compareReport", buildReportSummary(compareReport));
        export.put("compareResults", results);
        export.put("trendTags", trendTags);
        export.put("riskScore", getRiskScore(task.getId()));
        export.put("healthAdvices", listAdvices(task.getId()));
        return export;
    }

    /** 查询健康建议 */
    public List<HealthAdviceRecordEntity> healthAdvices(String taskNo) {
        ReportCompareTaskEntity task = getOwnedTask(taskNo);
        return listAdvices(task.getId());
    }

    private ExecutionContext executeCompare(ReportCompareTaskEntity task,
                                            ExamReportEntity baselineReport,
                                            ExamReportEntity compareReport) {
        List<ExamReportItemEntity> baselineItems = examReportItemMapper.selectList(
                new LambdaQueryWrapper<ExamReportItemEntity>()
                        .eq(ExamReportItemEntity::getReportId, baselineReport.getId())
                        .eq(ExamReportItemEntity::getIsDeleted, 0)
                        .orderByAsc(ExamReportItemEntity::getSortNo)
                        .orderByAsc(ExamReportItemEntity::getId));
        List<ExamReportItemEntity> compareItems = examReportItemMapper.selectList(
                new LambdaQueryWrapper<ExamReportItemEntity>()
                        .eq(ExamReportItemEntity::getReportId, compareReport.getId())
                        .eq(ExamReportItemEntity::getIsDeleted, 0)
                        .orderByAsc(ExamReportItemEntity::getSortNo)
                        .orderByAsc(ExamReportItemEntity::getId));

        Map<String, ExamReportItemEntity> baselineMap = new LinkedHashMap<String, ExamReportItemEntity>();
        for (ExamReportItemEntity item : baselineItems) {
            if (item.getItemCode() != null && !baselineMap.containsKey(item.getItemCode())) {
                baselineMap.put(item.getItemCode(), item);
            }
        }

        List<Map<String, Object>> incomparableItems = new ArrayList<Map<String, Object>>();
        List<ReportCompareResultEntity> compareResults = new ArrayList<ReportCompareResultEntity>();
        List<IndicatorTrendTagEntity> trendTags = new ArrayList<IndicatorTrendTagEntity>();
        List<HealthAdviceRecordEntity> advices = new ArrayList<HealthAdviceRecordEntity>();
        BigDecimal abnormalScore = ZERO;
        BigDecimal trendScore = ZERO;

        for (ExamReportItemEntity compareItem : compareItems) {
            ExamReportItemEntity baselineItem = baselineMap.get(compareItem.getItemCode());
            if (baselineItem == null) {
                continue;
            }
            if (baselineItem.getResultNumber() == null || compareItem.getResultNumber() == null) {
                continue;
            }
            if (!sameUnit(baselineItem.getUnit(), compareItem.getUnit())) {
                incomparableItems.add(buildIncomparableItem(baselineItem, compareItem, "单位不同，首期不纳入对比"));
                continue;
            }

            BigDecimal changeValue = compareItem.getResultNumber().subtract(baselineItem.getResultNumber());
            BigDecimal changeRate = null;
            if (baselineItem.getResultNumber().compareTo(ZERO) != 0) {
                changeRate = changeValue.divide(baselineItem.getResultNumber(), 4, RoundingMode.HALF_UP);
            }
            int trend = resolveTrend(changeValue);
            int riskLevel = resolveItemRiskLevel(compareItem, trend, changeRate);
            String trendTag = resolveTrendTag(compareItem, trend);
            String tagReason = buildTrendReason(baselineItem, compareItem, changeValue, changeRate, trendTag);

            ReportCompareResultEntity resultEntity = new ReportCompareResultEntity();
            resultEntity.setTaskId(task.getId());
            resultEntity.setItemCode(compareItem.getItemCode());
            resultEntity.setItemName(compareItem.getItemName());
            resultEntity.setBaseValue(baselineItem.getResultValue());
            resultEntity.setCompareValue(compareItem.getResultValue());
            resultEntity.setChangeValue(changeValue);
            resultEntity.setChangeRate(changeRate);
            resultEntity.setTrend(trend);
            resultEntity.setCreatedBy(AuthContext.getUserId());
            resultEntity.setUpdatedBy(AuthContext.getUserId());
            resultEntity.setIsDeleted(0);
            reportCompareResultMapper.insert(resultEntity);
            compareResults.add(resultEntity);

            IndicatorTrendTagEntity trendTagEntity = new IndicatorTrendTagEntity();
            trendTagEntity.setTaskId(task.getId());
            trendTagEntity.setItemCode(compareItem.getItemCode());
            trendTagEntity.setTrendTag(trendTag);
            trendTagEntity.setTrendDirection(trend);
            trendTagEntity.setRiskLevel(riskLevel);
            trendTagEntity.setTagReason(tagReason);
            trendTagEntity.setCreatedBy(AuthContext.getUserId());
            trendTagEntity.setUpdatedBy(AuthContext.getUserId());
            trendTagEntity.setIsDeleted(0);
            indicatorTrendTagMapper.insert(trendTagEntity);
            trendTags.add(trendTagEntity);

            abnormalScore = abnormalScore.add(resolveAbnormalScore(compareItem));
            trendScore = trendScore.add(resolveTrendScore(compareItem, trend, changeRate));

            HealthAdviceRecordEntity advice = buildAdvice(task, compareItem, trend, riskLevel, changeRate);
            if (advice != null) {
                healthAdviceRecordMapper.insert(advice);
                advices.add(advice);
            }
        }

        if (compareResults.isEmpty()) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "两份报告没有可对比的数值指标");
        }

        HealthRiskScoreEntity riskScore = new HealthRiskScoreEntity();
        riskScore.setTaskId(task.getId());
        riskScore.setUserId(task.getUserId());
        riskScore.setScoreAbnormal(abnormalScore.setScale(2, RoundingMode.HALF_UP));
        riskScore.setScoreTrend(trendScore.setScale(2, RoundingMode.HALF_UP));
        riskScore.setScoreTotal(abnormalScore.add(trendScore).setScale(2, RoundingMode.HALF_UP));
        riskScore.setRiskLevel(resolveTotalRiskLevel(riskScore.getScoreTotal()));
        riskScore.setScoreDetail(buildScoreDetail(compareResults.size(), incomparableItems.size(),
                riskScore.getScoreAbnormal(), riskScore.getScoreTrend()));
        riskScore.setCreatedBy(AuthContext.getUserId());
        riskScore.setUpdatedBy(AuthContext.getUserId());
        riskScore.setIsDeleted(0);
        healthRiskScoreMapper.insert(riskScore);

        ExecutionContext context = new ExecutionContext();
        context.results = compareResults;
        context.incomparableItems = incomparableItems;
        context.trendTags = trendTags;
        context.advices = advices;
        context.riskScore = riskScore;
        return context;
    }

    private ExamReportEntity getOwnedPublishedReport(String reportNo) {
        ExamReportEntity report = examReportMapper.selectOne(new LambdaQueryWrapper<ExamReportEntity>()
                .eq(ExamReportEntity::getReportNo, reportNo)
                .eq(ExamReportEntity::getUserId, AuthContext.getUserId())
                .eq(ExamReportEntity::getStatus, 3)
                .eq(ExamReportEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (report == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "报告不存在或未发布");
        }
        return report;
    }

    private ReportCompareTaskEntity getOwnedTask(String taskNo) {
        ReportCompareTaskEntity task = reportCompareTaskMapper.selectOne(new LambdaQueryWrapper<ReportCompareTaskEntity>()
                .eq(ReportCompareTaskEntity::getTaskNo, taskNo)
                .eq(ReportCompareTaskEntity::getUserId, AuthContext.getUserId())
                .eq(ReportCompareTaskEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (task == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "对比任务不存在");
        }
        return task;
    }

    private HealthRiskScoreEntity getRiskScore(Long taskId) {
        return healthRiskScoreMapper.selectOne(new LambdaQueryWrapper<HealthRiskScoreEntity>()
                .eq(HealthRiskScoreEntity::getTaskId, taskId)
                .eq(HealthRiskScoreEntity::getIsDeleted, 0)
                .last("limit 1"));
    }

    private List<HealthAdviceRecordEntity> listAdvices(Long taskId) {
        return healthAdviceRecordMapper.selectList(new LambdaQueryWrapper<HealthAdviceRecordEntity>()
                .eq(HealthAdviceRecordEntity::getCompareTaskId, taskId)
                .eq(HealthAdviceRecordEntity::getStatus, 1)
                .eq(HealthAdviceRecordEntity::getIsDeleted, 0)
                .orderByAsc(HealthAdviceRecordEntity::getId));
    }

    private Map<String, Object> buildReportSummary(ExamReportEntity report) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (report == null) {
            return result;
        }
        result.put("reportNo", report.getReportNo());
        result.put("reportDate", report.getReportDate());
        result.put("status", report.getStatus());
        result.put("packageId", report.getPackageId());
        result.put("overallConclusion", report.getOverallConclusion());
        return result;
    }

    private Map<String, Object> buildUserSummary(UserEntity user) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (user == null) {
            return result;
        }
        result.put("userId", user.getId());
        result.put("userNo", user.getUserNo());
        result.put("name", user.getName());
        result.put("gender", user.getGender());
        result.put("mobile", user.getMobile());
        return result;
    }

    private Map<String, Object> buildIncomparableItem(ExamReportItemEntity baselineItem,
                                                      ExamReportItemEntity compareItem,
                                                      String reason) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("itemCode", compareItem.getItemCode());
        result.put("itemName", compareItem.getItemName());
        result.put("baselineUnit", baselineItem.getUnit());
        result.put("compareUnit", compareItem.getUnit());
        result.put("reason", reason);
        return result;
    }

    private boolean sameUnit(String baselineUnit, String compareUnit) {
        String left = baselineUnit == null ? "" : baselineUnit.trim();
        String right = compareUnit == null ? "" : compareUnit.trim();
        return left.equals(right);
    }

    private int resolveTrend(BigDecimal changeValue) {
        int compare = changeValue.compareTo(ZERO);
        if (compare > 0) {
            return 1;
        }
        if (compare < 0) {
            return 2;
        }
        return 0;
    }

    private String resolveTrendTag(ExamReportItemEntity compareItem, int trend) {
        boolean abnormal = compareItem.getIsAbnormal() != null && compareItem.getIsAbnormal() == 1;
        if (trend == 1 && abnormal) {
            return "持续升高";
        }
        if (trend == 2 && abnormal) {
            return "下降未恢复";
        }
        if (trend == 0 && abnormal) {
            return "持续异常";
        }
        if (trend == 2) {
            return "改善恢复";
        }
        return "稳定正常";
    }

    private int resolveItemRiskLevel(ExamReportItemEntity compareItem, int trend, BigDecimal changeRate) {
        boolean abnormal = compareItem.getIsAbnormal() != null && compareItem.getIsAbnormal() == 1;
        if (abnormal && trend == 1) {
            return 2;
        }
        if (abnormal || (changeRate != null && changeRate.abs().compareTo(OBVIOUS_CHANGE_RATE) >= 0)) {
            return 1;
        }
        return 0;
    }

    private BigDecimal resolveAbnormalScore(ExamReportItemEntity compareItem) {
        if (compareItem.getIsAbnormal() == null || compareItem.getIsAbnormal() != 1) {
            return ZERO;
        }
        if (compareItem.getAbnormalLevel() != null && compareItem.getAbnormalLevel() >= 2) {
            return new BigDecimal("20");
        }
        return new BigDecimal("10");
    }

    private BigDecimal resolveTrendScore(ExamReportItemEntity compareItem, int trend, BigDecimal changeRate) {
        BigDecimal score = ZERO;
        if (trend == 1) {
            score = score.add(new BigDecimal("5"));
        }
        if (changeRate != null && changeRate.abs().compareTo(OBVIOUS_CHANGE_RATE) >= 0) {
            score = score.add(new BigDecimal("5"));
        }
        if (compareItem.getIsAbnormal() != null && compareItem.getIsAbnormal() == 1 && trend == 1) {
            score = score.add(new BigDecimal("10"));
        }
        return score;
    }

    private int resolveTotalRiskLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("30")) >= 0) {
            return 2;
        }
        if (totalScore.compareTo(new BigDecimal("10")) >= 0) {
            return 1;
        }
        return 0;
    }

    private String buildTrendReason(ExamReportItemEntity baselineItem,
                                    ExamReportItemEntity compareItem,
                                    BigDecimal changeValue,
                                    BigDecimal changeRate,
                                    String trendTag) {
        StringBuilder builder = new StringBuilder();
        builder.append(compareItem.getItemName()).append("较基线报告");
        if (changeValue.compareTo(ZERO) > 0) {
            builder.append("升高");
        } else if (changeValue.compareTo(ZERO) < 0) {
            builder.append("下降");
        } else {
            builder.append("持平");
        }
        builder.append("，趋势标签为").append(trendTag);
        if (changeRate != null) {
            builder.append("，变化率").append(changeRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)).append("%");
        }
        builder.append("。基线值=").append(baselineItem.getResultValue()).append("，当前值=").append(compareItem.getResultValue());
        return builder.toString();
    }

    private HealthAdviceRecordEntity buildAdvice(ReportCompareTaskEntity task,
                                                 ExamReportItemEntity compareItem,
                                                 int trend,
                                                 int riskLevel,
                                                 BigDecimal changeRate) {
        boolean abnormal = compareItem.getIsAbnormal() != null && compareItem.getIsAbnormal() == 1;
        String adviceType;
        String title;
        String content;
        String actionSuggestion;

        if (abnormal && trend == 1) {
            adviceType = "FOLLOWUP";
            title = compareItem.getItemName() + "建议复查";
            content = compareItem.getItemName() + "当前异常且较上次升高，建议尽快复查并咨询医生评估。";
            actionSuggestion = "建议1-2周内复查或到相关专科就诊";
        } else if (abnormal) {
            adviceType = "ABNORMAL";
            title = compareItem.getItemName() + "持续观察";
            content = compareItem.getItemName() + "当前仍有异常表现，虽未继续恶化，但仍建议结合医生意见持续观察。";
            actionSuggestion = "按医嘱复诊，保持指标监测";
        } else if (changeRate != null && changeRate.abs().compareTo(OBVIOUS_CHANGE_RATE) >= 0) {
            adviceType = "TREND";
            title = compareItem.getItemName() + "生活方式干预";
            content = compareItem.getItemName() + "目前虽未见异常，但波动较明显，建议调整作息、饮食和运动习惯。";
            actionSuggestion = "3个月内复查一次并观察变化";
        } else if (trend == 0) {
            adviceType = "TREND";
            title = compareItem.getItemName() + "保持稳定";
            content = compareItem.getItemName() + "本次与基线相比保持稳定，建议继续保持当前健康管理习惯。";
            actionSuggestion = "按年度体检计划继续复查";
        } else {
            return null;
        }

        HealthAdviceRecordEntity advice = new HealthAdviceRecordEntity();
        advice.setAdviceNo(NoGenerator.next("ADV"));
        advice.setUserId(task.getUserId());
        advice.setReportId(null);
        advice.setCompareTaskId(task.getId());
        advice.setSourceType("COMPARE_TASK");
        advice.setAdviceType(adviceType);
        advice.setRiskLevel(riskLevel);
        advice.setAdviceTitle(title);
        advice.setAdviceContent(content);
        advice.setActionSuggestion(actionSuggestion);
        advice.setStatus(1);
        advice.setCreatedBy(AuthContext.getUserId());
        advice.setUpdatedBy(AuthContext.getUserId());
        advice.setIsDeleted(0);
        return advice;
    }

    private String buildScoreDetail(int comparableCount,
                                    int incomparableCount,
                                    BigDecimal scoreAbnormal,
                                    BigDecimal scoreTrend) {
        return "{\"comparableCount\":" + comparableCount
                + ",\"incomparableCount\":" + incomparableCount
                + ",\"scoreAbnormal\":" + scoreAbnormal
                + ",\"scoreTrend\":" + scoreTrend + "}";
    }

    private static class ExecutionContext {
        private List<ReportCompareResultEntity> results;
        private List<Map<String, Object>> incomparableItems;
        private List<IndicatorTrendTagEntity> trendTags;
        private List<HealthAdviceRecordEntity> advices;
        private HealthRiskScoreEntity riskScore;
    }
}
