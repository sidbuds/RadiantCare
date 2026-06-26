package com.xixin.health.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.consultation.entity.DoctorConsultationReplyEntity;
import com.xixin.health.consultation.mapper.DoctorConsultationReplyMapper;
import com.xixin.health.exam.entity.ExamResultEntity;
import com.xixin.health.exam.entity.ExamTaskItemEntity;
import com.xixin.health.exam.mapper.ExamResultMapper;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import com.xixin.health.report.entity.DoctorReviewRecordEntity;
import com.xixin.health.report.mapper.DoctorReviewRecordMapper;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生端数据分析服务
 */
@Service
public class DoctorAnalyticsService {

    private final ExamResultMapper examResultMapper;
    private final ExamTaskItemMapper examTaskItemMapper;
    private final DoctorReviewRecordMapper doctorReviewRecordMapper;
    private final DoctorConsultationReplyMapper doctorConsultationReplyMapper;
    private final UserMapper userMapper;

    public DoctorAnalyticsService(ExamResultMapper examResultMapper,
                                  ExamTaskItemMapper examTaskItemMapper,
                                  DoctorReviewRecordMapper doctorReviewRecordMapper,
                                  DoctorConsultationReplyMapper doctorConsultationReplyMapper,
                                  UserMapper userMapper) {
        this.examResultMapper = examResultMapper;
        this.examTaskItemMapper = examTaskItemMapper;
        this.doctorReviewRecordMapper = doctorReviewRecordMapper;
        this.doctorConsultationReplyMapper = doctorConsultationReplyMapper;
        this.userMapper = userMapper;
    }

    public Map<String, Object> abnormalOverview(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, null);
        long totalCount = results.size();
        long abnormalCount = countAbnormal(results);
        long highRiskCount = countHighRisk(results);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("doctorId", doctorId);
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("totalResultCount", totalCount);
        result.put("abnormalCount", abnormalCount);
        result.put("abnormalRate", ratio(abnormalCount, totalCount));
        result.put("highRiskCount", highRiskCount);
        result.put("highRiskRate", ratio(highRiskCount, totalCount));
        return result;
    }

    public List<Map<String, Object>> abnormalDistribution(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, null);
        Map<String, DistributionCounter> grouped = new LinkedHashMap<String, DistributionCounter>();
        for (ExamResultEntity item : results) {
            String key = item.getItemCode();
            DistributionCounter counter = grouped.get(key);
            if (counter == null) {
                counter = new DistributionCounter(item.getItemCode(), item.getItemName());
                grouped.put(key, counter);
            }
            counter.totalCount++;
            if (isAbnormal(item)) {
                counter.abnormalCount++;
            }
            if (isHighRisk(item)) {
                counter.highRiskCount++;
            }
            int level = item.getAbnormalLevel() == null ? 0 : item.getAbnormalLevel();
            if (level == 1) {
                counter.level1Count++;
            } else if (level == 2) {
                counter.level2Count++;
            } else if (level >= 3) {
                counter.level3Count++;
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DistributionCounter item : grouped.values()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("itemCode", item.itemCode);
            row.put("itemName", item.itemName);
            row.put("totalCount", item.totalCount);
            row.put("abnormalCount", item.abnormalCount);
            row.put("highRiskCount", item.highRiskCount);
            row.put("level1Count", item.level1Count);
            row.put("level2Count", item.level2Count);
            row.put("level3Count", item.level3Count);
            row.put("abnormalRate", ratio(item.abnormalCount, item.totalCount));
            result.add(row);
        }
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                long leftValue = ((Number) left.get("abnormalCount")).longValue();
                long rightValue = ((Number) right.get("abnormalCount")).longValue();
                return Long.compare(rightValue, leftValue);
            }
        });
        return result;
    }

    public List<Map<String, Object>> highRiskUsers(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, null);
        Map<Long, Map<String, Object>> grouped = new LinkedHashMap<Long, Map<String, Object>>();
        for (ExamResultEntity item : results) {
            if (!isHighRisk(item) || item.getUserId() == null) {
                continue;
            }
            Map<String, Object> row = grouped.get(item.getUserId());
            if (row == null) {
                UserEntity user = userMapper.selectById(item.getUserId());
                row = new LinkedHashMap<String, Object>();
                row.put("userId", item.getUserId());
                row.put("userName", user == null ? null : user.getName());
                row.put("mobile", user == null ? null : user.getMobile());
                row.put("highRiskItemCount", 0);
                row.put("maxAbnormalLevel", 0);
                row.put("latestEntryTime", item.getEntryTime());
                row.put("items", new ArrayList<Map<String, Object>>());
                grouped.put(item.getUserId(), row);
            }
            row.put("highRiskItemCount", ((Integer) row.get("highRiskItemCount")) + 1);
            int currentLevel = item.getAbnormalLevel() == null ? 0 : item.getAbnormalLevel();
            row.put("maxAbnormalLevel", Math.max(((Integer) row.get("maxAbnormalLevel")), currentLevel));
            LocalDateTime latestEntryTime = (LocalDateTime) row.get("latestEntryTime");
            if (latestEntryTime == null || (item.getEntryTime() != null && item.getEntryTime().isAfter(latestEntryTime))) {
                row.put("latestEntryTime", item.getEntryTime());
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) row.get("items");
            Map<String, Object> detail = new LinkedHashMap<String, Object>();
            detail.put("itemCode", item.getItemCode());
            detail.put("itemName", item.getItemName());
            detail.put("resultValue", item.getResultValue());
            detail.put("resultNumber", item.getResultNumber());
            detail.put("unit", item.getUnit());
            detail.put("abnormalLevel", item.getAbnormalLevel());
            detail.put("conclusion", item.getConclusion());
            detail.put("entryTime", item.getEntryTime());
            items.add(detail);
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(grouped.values());
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                int levelCompare = Integer.compare(((Integer) right.get("maxAbnormalLevel")), ((Integer) left.get("maxAbnormalLevel")));
                if (levelCompare != 0) {
                    return levelCompare;
                }
                return Integer.compare(((Integer) right.get("highRiskItemCount")), ((Integer) left.get("highRiskItemCount")));
            }
        });
        return result;
    }

    public List<Map<String, Object>> abnormalTrend(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 6);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, null);
        Map<LocalDate, Map<String, Object>> grouped = initDailyRange(start, end);
        for (ExamResultEntity item : results) {
            if (item.getEntryTime() == null) {
                continue;
            }
            LocalDate date = item.getEntryTime().toLocalDate();
            Map<String, Object> row = grouped.get(date);
            if (row == null) {
                continue;
            }
            row.put("totalCount", ((Integer) row.get("totalCount")) + 1);
            if (isAbnormal(item)) {
                row.put("abnormalCount", ((Integer) row.get("abnormalCount")) + 1);
            }
            if (isHighRisk(item)) {
                row.put("highRiskCount", ((Integer) row.get("highRiskCount")) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(grouped.values());
        Integer prevAbnormalCount = null;
        for (Map<String, Object> row : result) {
            int abnormalCount = (Integer) row.get("abnormalCount");
            row.put("abnormalRate", ratio(abnormalCount, ((Integer) row.get("totalCount"))));
            row.put("momDelta", prevAbnormalCount == null ? null : abnormalCount - prevAbnormalCount);
            prevAbnormalCount = abnormalCount;
        }
        return result;
    }

    public Map<String, Object> workloadOverview(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamTaskItemEntity> taskItems = loadTaskItems(start, end, doctorId, null);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, null);
        List<DoctorReviewRecordEntity> reviews = loadReviews(start, end, doctorId);
        List<DoctorConsultationReplyEntity> replies = loadConsultationReplies(start, end, doctorId);

        long completedItemCount = countCompletedTaskItems(taskItems);
        long resultEntryCount = results.size();
        long reviewCount = reviews.size();
        long consultationReplyCount = replies.size();

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("doctorId", doctorId);
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("completedItemCount", completedItemCount);
        result.put("resultEntryCount", resultEntryCount);
        result.put("reviewCount", reviewCount);
        result.put("consultationReplyCount", consultationReplyCount);
        result.put("avgCompletionMinutes", averageCompletionMinutes(taskItems));
        return result;
    }

    public List<Map<String, Object>> workloadTrend(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 6);
        LocalDate end = resolveEnd(endDate);
        List<ExamTaskItemEntity> taskItems = loadTaskItems(start, end, doctorId, null);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, null);
        List<DoctorReviewRecordEntity> reviews = loadReviews(start, end, doctorId);
        List<DoctorConsultationReplyEntity> replies = loadConsultationReplies(start, end, doctorId);
        Map<LocalDate, Map<String, Object>> grouped = initWorkloadDailyRange(start, end);

        for (ExamTaskItemEntity item : taskItems) {
            if (item.getCompleteTime() != null && item.getItemStatus() != null && item.getItemStatus() == 2) {
                Map<String, Object> row = grouped.get(item.getCompleteTime().toLocalDate());
                if (row != null) {
                    row.put("completedItemCount", ((Integer) row.get("completedItemCount")) + 1);
                }
            }
        }
        for (ExamResultEntity item : results) {
            if (item.getEntryTime() != null) {
                Map<String, Object> row = grouped.get(item.getEntryTime().toLocalDate());
                if (row != null) {
                    row.put("resultEntryCount", ((Integer) row.get("resultEntryCount")) + 1);
                }
            }
        }
        for (DoctorReviewRecordEntity item : reviews) {
            if (item.getReviewedAt() != null) {
                Map<String, Object> row = grouped.get(item.getReviewedAt().toLocalDate());
                if (row != null) {
                    row.put("reviewCount", ((Integer) row.get("reviewCount")) + 1);
                }
            }
        }
        for (DoctorConsultationReplyEntity item : replies) {
            if (item.getReplyTime() != null) {
                Map<String, Object> row = grouped.get(item.getReplyTime().toLocalDate());
                if (row != null) {
                    row.put("consultationReplyCount", ((Integer) row.get("consultationReplyCount")) + 1);
                }
            }
        }
        return new ArrayList<Map<String, Object>>(grouped.values());
    }

    public List<Map<String, Object>> workloadBreakdown(LocalDate startDate, LocalDate endDate) {
        Long doctorId = AuthContext.getUserId();
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamTaskItemEntity> taskItems = loadTaskItems(start, end, doctorId, null);
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<String, Map<String, Object>>();
        for (ExamTaskItemEntity item : taskItems) {
            String key = item.getDepartmentCode() + "#" + item.getItemCode();
            Map<String, Object> row = grouped.get(key);
            if (row == null) {
                row = new LinkedHashMap<String, Object>();
                row.put("departmentCode", item.getDepartmentCode());
                row.put("departmentName", item.getDepartmentName());
                row.put("itemCode", item.getItemCode());
                row.put("itemName", item.getItemName());
                row.put("taskItemCount", 0);
                row.put("completedItemCount", 0);
                row.put("avgCompletionMinutes", BigDecimal.ZERO);
                row.put("completionSamples", 0);
                grouped.put(key, row);
            }
            row.put("taskItemCount", ((Integer) row.get("taskItemCount")) + 1);
            if (item.getItemStatus() != null && item.getItemStatus() == 2) {
                row.put("completedItemCount", ((Integer) row.get("completedItemCount")) + 1);
            }
            if (item.getStartTime() != null && item.getCompleteTime() != null && !item.getCompleteTime().isBefore(item.getStartTime())) {
                Duration duration = Duration.between(item.getStartTime(), item.getCompleteTime());
                BigDecimal totalMinutes = ((BigDecimal) row.get("avgCompletionMinutes"))
                        .multiply(new BigDecimal((Integer) row.get("completionSamples")))
                        .add(new BigDecimal(duration.toMinutes()));
                int samples = ((Integer) row.get("completionSamples")) + 1;
                row.put("completionSamples", samples);
                row.put("avgCompletionMinutes", totalMinutes.divide(new BigDecimal(samples), 2, RoundingMode.HALF_UP));
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : grouped.values()) {
            row.remove("completionSamples");
            result.add(row);
        }
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                return Integer.compare(((Integer) right.get("completedItemCount")), ((Integer) left.get("completedItemCount")));
            }
        });
        return result;
    }

    public Map<String, Object> abnormalOverviewForAdmin(LocalDate startDate, LocalDate endDate, Long doctorId, String departmentCode) {
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, departmentCode);
        long totalCount = results.size();
        long abnormalCount = countAbnormal(results);
        long highRiskCount = countHighRisk(results);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("doctorId", doctorId);
        result.put("departmentCode", departmentCode);
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("totalResultCount", totalCount);
        result.put("abnormalCount", abnormalCount);
        result.put("abnormalRate", ratio(abnormalCount, totalCount));
        result.put("highRiskCount", highRiskCount);
        result.put("highRiskRate", ratio(highRiskCount, totalCount));
        return result;
    }

    public List<Map<String, Object>> abnormalDistributionForAdmin(LocalDate startDate, LocalDate endDate, Long doctorId, String departmentCode) {
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, departmentCode);
        Map<String, DistributionCounter> grouped = new LinkedHashMap<String, DistributionCounter>();
        for (ExamResultEntity item : results) {
            String key = item.getItemCode();
            DistributionCounter counter = grouped.get(key);
            if (counter == null) {
                counter = new DistributionCounter(item.getItemCode(), item.getItemName());
                grouped.put(key, counter);
            }
            counter.totalCount++;
            if (isAbnormal(item)) {
                counter.abnormalCount++;
            }
            if (isHighRisk(item)) {
                counter.highRiskCount++;
            }
            int level = item.getAbnormalLevel() == null ? 0 : item.getAbnormalLevel();
            if (level == 1) {
                counter.level1Count++;
            } else if (level == 2) {
                counter.level2Count++;
            } else if (level >= 3) {
                counter.level3Count++;
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DistributionCounter item : grouped.values()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("itemCode", item.itemCode);
            row.put("itemName", item.itemName);
            row.put("totalCount", item.totalCount);
            row.put("abnormalCount", item.abnormalCount);
            row.put("highRiskCount", item.highRiskCount);
            row.put("level1Count", item.level1Count);
            row.put("level2Count", item.level2Count);
            row.put("level3Count", item.level3Count);
            row.put("abnormalRate", ratio(item.abnormalCount, item.totalCount));
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> highRiskUsersForAdmin(LocalDate startDate, LocalDate endDate, Long doctorId, String departmentCode) {
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamResultEntity> results = loadExamResults(start, end, doctorId, departmentCode);
        Map<Long, Map<String, Object>> grouped = new LinkedHashMap<Long, Map<String, Object>>();
        for (ExamResultEntity item : results) {
            if (!isHighRisk(item) || item.getUserId() == null) {
                continue;
            }
            Map<String, Object> row = grouped.get(item.getUserId());
            if (row == null) {
                UserEntity user = userMapper.selectById(item.getUserId());
                row = new LinkedHashMap<String, Object>();
                row.put("userId", item.getUserId());
                row.put("userName", user == null ? null : user.getName());
                row.put("mobile", user == null ? null : user.getMobile());
                row.put("highRiskItemCount", 0);
                row.put("maxAbnormalLevel", 0);
                row.put("latestEntryTime", item.getEntryTime());
                row.put("doctorId", item.getEntryDoctorId());
                row.put("doctorName", item.getEntryDoctorName());
                row.put("items", new ArrayList<Map<String, Object>>());
                grouped.put(item.getUserId(), row);
            }
            row.put("highRiskItemCount", ((Integer) row.get("highRiskItemCount")) + 1);
            row.put("maxAbnormalLevel", Math.max(((Integer) row.get("maxAbnormalLevel")), item.getAbnormalLevel() == null ? 0 : item.getAbnormalLevel()));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) row.get("items");
            Map<String, Object> detail = new LinkedHashMap<String, Object>();
            detail.put("itemCode", item.getItemCode());
            detail.put("itemName", item.getItemName());
            detail.put("abnormalLevel", item.getAbnormalLevel());
            detail.put("entryTime", item.getEntryTime());
            items.add(detail);
        }
        return new ArrayList<Map<String, Object>>(grouped.values());
    }

    public List<Map<String, Object>> workloadRanking(LocalDate startDate, LocalDate endDate, String departmentCode) {
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamTaskItemEntity> taskItems = loadTaskItems(start, end, null, departmentCode);
        List<ExamResultEntity> results = loadExamResults(start, end, null, departmentCode);
        List<DoctorReviewRecordEntity> reviews = loadReviews(start, end, null);
        List<DoctorConsultationReplyEntity> replies = loadConsultationReplies(start, end, null);
        Map<Long, Map<String, Object>> grouped = new LinkedHashMap<Long, Map<String, Object>>();

        for (ExamTaskItemEntity item : taskItems) {
            if (item.getDoctorId() == null) {
                continue;
            }
            Map<String, Object> row = ensureDoctorRow(grouped, item.getDoctorId(), item.getDoctorName(), item.getDepartmentCode(), item.getDepartmentName());
            row.put("taskItemCount", ((Integer) row.get("taskItemCount")) + 1);
            if (item.getItemStatus() != null && item.getItemStatus() == 2) {
                row.put("completedItemCount", ((Integer) row.get("completedItemCount")) + 1);
            }
            appendDuration(row, item.getStartTime(), item.getCompleteTime());
        }
        for (ExamResultEntity item : results) {
            if (item.getEntryDoctorId() == null) {
                continue;
            }
            Map<String, Object> row = ensureDoctorRow(grouped, item.getEntryDoctorId(), item.getEntryDoctorName(), null, null);
            row.put("resultEntryCount", ((Integer) row.get("resultEntryCount")) + 1);
            if (isAbnormal(item)) {
                row.put("abnormalCount", ((Integer) row.get("abnormalCount")) + 1);
            }
            if (isHighRisk(item)) {
                row.put("highRiskCount", ((Integer) row.get("highRiskCount")) + 1);
            }
        }
        for (DoctorReviewRecordEntity item : reviews) {
            if (item.getReviewerId() == null) {
                continue;
            }
            Map<String, Object> row = ensureDoctorRow(grouped, item.getReviewerId(), item.getReviewerName(), null, null);
            row.put("reviewCount", ((Integer) row.get("reviewCount")) + 1);
        }
        for (DoctorConsultationReplyEntity item : replies) {
            if (item.getReplyUserId() == null) {
                continue;
            }
            Map<String, Object> row = ensureDoctorRow(grouped, item.getReplyUserId(), item.getReplyUserName(), null, null);
            row.put("consultationReplyCount", ((Integer) row.get("consultationReplyCount")) + 1);
        }

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : grouped.values()) {
            finalizeDuration(row);
            result.add(row);
        }
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                int leftScore = score(left);
                int rightScore = score(right);
                return Integer.compare(rightScore, leftScore);
            }
        });
        return result;
    }

    public List<Map<String, Object>> departmentWorkload(LocalDate startDate, LocalDate endDate) {
        LocalDate start = resolveStart(startDate, 29);
        LocalDate end = resolveEnd(endDate);
        List<ExamTaskItemEntity> taskItems = loadTaskItems(start, end, null, null);
        List<ExamResultEntity> results = loadExamResults(start, end, null, null);
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<String, Map<String, Object>>();
        for (ExamTaskItemEntity item : taskItems) {
            String key = item.getDepartmentCode() == null ? "UNKNOWN" : item.getDepartmentCode();
            Map<String, Object> row = grouped.get(key);
            if (row == null) {
                row = new LinkedHashMap<String, Object>();
                row.put("departmentCode", item.getDepartmentCode());
                row.put("departmentName", item.getDepartmentName());
                row.put("taskItemCount", 0);
                row.put("completedItemCount", 0);
                row.put("resultEntryCount", 0);
                row.put("abnormalCount", 0);
                grouped.put(key, row);
            }
            row.put("taskItemCount", ((Integer) row.get("taskItemCount")) + 1);
            if (item.getItemStatus() != null && item.getItemStatus() == 2) {
                row.put("completedItemCount", ((Integer) row.get("completedItemCount")) + 1);
            }
        }
        for (ExamResultEntity item : results) {
            String key = resolveDepartmentKeyFromResults(taskItems, item.getTaskItemId());
            Map<String, Object> row = grouped.get(key);
            if (row == null) {
                row = new LinkedHashMap<String, Object>();
                row.put("departmentCode", key);
                row.put("departmentName", key);
                row.put("taskItemCount", 0);
                row.put("completedItemCount", 0);
                row.put("resultEntryCount", 0);
                row.put("abnormalCount", 0);
                grouped.put(key, row);
            }
            row.put("resultEntryCount", ((Integer) row.get("resultEntryCount")) + 1);
            if (isAbnormal(item)) {
                row.put("abnormalCount", ((Integer) row.get("abnormalCount")) + 1);
            }
        }
        return new ArrayList<Map<String, Object>>(grouped.values());
    }

    private LocalDate resolveStart(LocalDate startDate, int defaultDays) {
        return startDate == null ? LocalDate.now().minusDays(defaultDays) : startDate;
    }

    private LocalDate resolveEnd(LocalDate endDate) {
        return endDate == null ? LocalDate.now() : endDate;
    }

    private List<ExamResultEntity> loadExamResults(LocalDate start, LocalDate end, Long doctorId, String departmentCode) {
        List<ExamResultEntity> results = examResultMapper.selectList(new LambdaQueryWrapper<ExamResultEntity>()
                .ge(ExamResultEntity::getEntryTime, start.atStartOfDay())
                .lt(ExamResultEntity::getEntryTime, end.plusDays(1).atStartOfDay())
                .eq(doctorId != null, ExamResultEntity::getEntryDoctorId, doctorId)
                .eq(ExamResultEntity::getIsDeleted, 0)
                .orderByAsc(ExamResultEntity::getEntryTime));
        if (departmentCode == null || departmentCode.trim().isEmpty()) {
            return results;
        }
        Map<Long, ExamTaskItemEntity> taskItemMap = loadTaskItemMapByIds(results);
        List<ExamResultEntity> filtered = new ArrayList<ExamResultEntity>();
        for (ExamResultEntity item : results) {
            ExamTaskItemEntity taskItem = taskItemMap.get(item.getTaskItemId());
            if (taskItem != null && departmentCode.equals(taskItem.getDepartmentCode())) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<ExamTaskItemEntity> loadTaskItems(LocalDate start, LocalDate end, Long doctorId, String departmentCode) {
        return examTaskItemMapper.selectList(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .ge(ExamTaskItemEntity::getCreatedAt, start.atStartOfDay())
                .lt(ExamTaskItemEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .eq(doctorId != null, ExamTaskItemEntity::getDoctorId, doctorId)
                .eq(departmentCode != null && !departmentCode.trim().isEmpty(), ExamTaskItemEntity::getDepartmentCode, departmentCode)
                .eq(ExamTaskItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamTaskItemEntity::getCreatedAt));
    }

    private List<DoctorReviewRecordEntity> loadReviews(LocalDate start, LocalDate end, Long reviewerId) {
        return doctorReviewRecordMapper.selectList(new LambdaQueryWrapper<DoctorReviewRecordEntity>()
                .ge(DoctorReviewRecordEntity::getReviewedAt, start.atStartOfDay())
                .lt(DoctorReviewRecordEntity::getReviewedAt, end.plusDays(1).atStartOfDay())
                .eq(reviewerId != null, DoctorReviewRecordEntity::getReviewerId, reviewerId)
                .eq(DoctorReviewRecordEntity::getIsDeleted, 0)
                .orderByAsc(DoctorReviewRecordEntity::getReviewedAt));
    }

    private List<DoctorConsultationReplyEntity> loadConsultationReplies(LocalDate start, LocalDate end, Long replyUserId) {
        return doctorConsultationReplyMapper.selectList(new LambdaQueryWrapper<DoctorConsultationReplyEntity>()
                .ge(DoctorConsultationReplyEntity::getReplyTime, start.atStartOfDay())
                .lt(DoctorConsultationReplyEntity::getReplyTime, end.plusDays(1).atStartOfDay())
                .eq(replyUserId != null, DoctorConsultationReplyEntity::getReplyUserId, replyUserId)
                .eq(DoctorConsultationReplyEntity::getReplyRole, "DOCTOR")
                .eq(DoctorConsultationReplyEntity::getIsDeleted, 0)
                .orderByAsc(DoctorConsultationReplyEntity::getReplyTime));
    }

    private Map<LocalDate, Map<String, Object>> initDailyRange(LocalDate start, LocalDate end) {
        Map<LocalDate, Map<String, Object>> result = new LinkedHashMap<LocalDate, Map<String, Object>>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("date", current);
            row.put("totalCount", 0);
            row.put("abnormalCount", 0);
            row.put("highRiskCount", 0);
            result.put(current, row);
            current = current.plusDays(1);
        }
        return result;
    }

    private Map<LocalDate, Map<String, Object>> initWorkloadDailyRange(LocalDate start, LocalDate end) {
        Map<LocalDate, Map<String, Object>> result = new LinkedHashMap<LocalDate, Map<String, Object>>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("date", current);
            row.put("completedItemCount", 0);
            row.put("resultEntryCount", 0);
            row.put("reviewCount", 0);
            row.put("consultationReplyCount", 0);
            result.put(current, row);
            current = current.plusDays(1);
        }
        return result;
    }

    private long countAbnormal(List<ExamResultEntity> results) {
        long count = 0;
        for (ExamResultEntity item : results) {
            if (isAbnormal(item)) {
                count++;
            }
        }
        return count;
    }

    private long countHighRisk(List<ExamResultEntity> results) {
        long count = 0;
        for (ExamResultEntity item : results) {
            if (isHighRisk(item)) {
                count++;
            }
        }
        return count;
    }

    private long countCompletedTaskItems(List<ExamTaskItemEntity> taskItems) {
        long count = 0;
        for (ExamTaskItemEntity item : taskItems) {
            if (item.getItemStatus() != null && item.getItemStatus() == 2) {
                count++;
            }
        }
        return count;
    }

    private BigDecimal averageCompletionMinutes(List<ExamTaskItemEntity> taskItems) {
        long totalMinutes = 0;
        long sampleCount = 0;
        for (ExamTaskItemEntity item : taskItems) {
            if (item.getStartTime() == null || item.getCompleteTime() == null || item.getCompleteTime().isBefore(item.getStartTime())) {
                continue;
            }
            totalMinutes += Duration.between(item.getStartTime(), item.getCompleteTime()).toMinutes();
            sampleCount++;
        }
        if (sampleCount == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(totalMinutes).divide(new BigDecimal(sampleCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), 4, RoundingMode.HALF_UP);
    }

    private boolean isAbnormal(ExamResultEntity item) {
        return item.getIsAbnormal() != null && item.getIsAbnormal() == 1;
    }

    private boolean isHighRisk(ExamResultEntity item) {
        return isAbnormal(item) && item.getAbnormalLevel() != null && item.getAbnormalLevel() >= 2;
    }

    private Map<Long, ExamTaskItemEntity> loadTaskItemMapByIds(List<ExamResultEntity> results) {
        Map<Long, ExamTaskItemEntity> map = new HashMap<Long, ExamTaskItemEntity>();
        for (ExamResultEntity item : results) {
            if (item.getTaskItemId() == null || map.containsKey(item.getTaskItemId())) {
                continue;
            }
            ExamTaskItemEntity taskItem = examTaskItemMapper.selectById(item.getTaskItemId());
            if (taskItem != null) {
                map.put(item.getTaskItemId(), taskItem);
            }
        }
        return map;
    }

    private Map<String, Object> ensureDoctorRow(Map<Long, Map<String, Object>> grouped,
                                                Long doctorId,
                                                String doctorName,
                                                String departmentCode,
                                                String departmentName) {
        Map<String, Object> row = grouped.get(doctorId);
        if (row == null) {
            row = new LinkedHashMap<String, Object>();
            row.put("doctorId", doctorId);
            row.put("doctorName", doctorName);
            row.put("departmentCode", departmentCode);
            row.put("departmentName", departmentName);
            row.put("taskItemCount", 0);
            row.put("completedItemCount", 0);
            row.put("resultEntryCount", 0);
            row.put("reviewCount", 0);
            row.put("consultationReplyCount", 0);
            row.put("abnormalCount", 0);
            row.put("highRiskCount", 0);
            row.put("durationTotalMinutes", 0L);
            row.put("durationSampleCount", 0);
            grouped.put(doctorId, row);
        } else {
            if (row.get("doctorName") == null && doctorName != null) {
                row.put("doctorName", doctorName);
            }
            if (row.get("departmentCode") == null && departmentCode != null) {
                row.put("departmentCode", departmentCode);
            }
            if (row.get("departmentName") == null && departmentName != null) {
                row.put("departmentName", departmentName);
            }
        }
        return row;
    }

    private void appendDuration(Map<String, Object> row, LocalDateTime startTime, LocalDateTime completeTime) {
        if (startTime == null || completeTime == null || completeTime.isBefore(startTime)) {
            return;
        }
        row.put("durationTotalMinutes", ((Long) row.get("durationTotalMinutes")) + Duration.between(startTime, completeTime).toMinutes());
        row.put("durationSampleCount", ((Integer) row.get("durationSampleCount")) + 1);
    }

    private void finalizeDuration(Map<String, Object> row) {
        long totalMinutes = (Long) row.remove("durationTotalMinutes");
        int sampleCount = (Integer) row.remove("durationSampleCount");
        if (sampleCount <= 0) {
            row.put("avgCompletionMinutes", BigDecimal.ZERO);
        } else {
            row.put("avgCompletionMinutes", new BigDecimal(totalMinutes).divide(new BigDecimal(sampleCount), 2, RoundingMode.HALF_UP));
        }
    }

    private int score(Map<String, Object> row) {
        return ((Integer) row.get("completedItemCount"))
                + ((Integer) row.get("resultEntryCount"))
                + ((Integer) row.get("reviewCount"))
                + ((Integer) row.get("consultationReplyCount"));
    }

    private String resolveDepartmentKeyFromResults(List<ExamTaskItemEntity> taskItems, Long taskItemId) {
        if (taskItemId == null) {
            return "UNKNOWN";
        }
        for (ExamTaskItemEntity item : taskItems) {
            if (taskItemId.equals(item.getId())) {
                return item.getDepartmentCode() == null ? "UNKNOWN" : item.getDepartmentCode();
            }
        }
        return "UNKNOWN";
    }

    private static class DistributionCounter {
        private final String itemCode;
        private final String itemName;
        private int totalCount;
        private int abnormalCount;
        private int highRiskCount;
        private int level1Count;
        private int level2Count;
        private int level3Count;

        private DistributionCounter(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
        }
    }
}
