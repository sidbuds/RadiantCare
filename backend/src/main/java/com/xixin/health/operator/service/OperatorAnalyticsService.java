package com.xixin.health.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageMapper;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.entity.RefundApplyEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.order.mapper.RefundApplyMapper;
import com.xixin.health.report.entity.ExamReportEntity;
import com.xixin.health.report.mapper.ExamReportMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperatorAnalyticsService {

    private final AppointmentMapper appointmentMapper;
    private final OrderMapper orderMapper;
    private final RefundApplyMapper refundApplyMapper;
    private final ExamReportMapper examReportMapper;
    private final ExamPackageMapper examPackageMapper;

    public OperatorAnalyticsService(AppointmentMapper appointmentMapper,
                                    OrderMapper orderMapper,
                                    RefundApplyMapper refundApplyMapper,
                                    ExamReportMapper examReportMapper,
                                    ExamPackageMapper examPackageMapper) {
        this.appointmentMapper = appointmentMapper;
        this.orderMapper = orderMapper;
        this.refundApplyMapper = refundApplyMapper;
        this.examReportMapper = examReportMapper;
        this.examPackageMapper = examPackageMapper;
    }

    public Map<String, Object> dashboard(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.now().minusDays(29) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        long appointmentCount = appointmentMapper.selectCount(new LambdaQueryWrapper<AppointmentEntity>()
                .ge(AppointmentEntity::getAppointDate, start)
                .le(AppointmentEntity::getAppointDate, end)
                .eq(AppointmentEntity::getIsDeleted, 0));
        long paidOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                .ge(OrderEntity::getCreatedAt, start.atStartOfDay())
                .lt(OrderEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .in(OrderEntity::getStatus, 1, 2, 3, 4)
                .eq(OrderEntity::getIsDeleted, 0));
        long refundCount = refundApplyMapper.selectCount(new LambdaQueryWrapper<RefundApplyEntity>()
                .ge(RefundApplyEntity::getCreatedAt, start.atStartOfDay())
                .lt(RefundApplyEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .eq(RefundApplyEntity::getIsDeleted, 0));
        long publishedReportCount = examReportMapper.selectCount(new LambdaQueryWrapper<ExamReportEntity>()
                .ge(ExamReportEntity::getPublishedAt, start.atStartOfDay())
                .lt(ExamReportEntity::getPublishedAt, end.plusDays(1).atStartOfDay())
                .eq(ExamReportEntity::getStatus, 3)
                .eq(ExamReportEntity::getIsDeleted, 0));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("appointmentCount", appointmentCount);
        result.put("paidOrderCount", paidOrderCount);
        result.put("refundCount", refundCount);
        result.put("publishedReportCount", publishedReportCount);
        result.put("refundRate", paidOrderCount == 0 ? BigDecimal.ZERO :
                new BigDecimal(refundCount).divide(new BigDecimal(paidOrderCount), 4, BigDecimal.ROUND_HALF_UP));
        return result;
    }

    public List<Map<String, Object>> appointmentTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.now().minusDays(6) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        List<AppointmentEntity> appointments = appointmentMapper.selectList(new LambdaQueryWrapper<AppointmentEntity>()
                .ge(AppointmentEntity::getCreatedAt, start.atStartOfDay())
                .lt(AppointmentEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .eq(AppointmentEntity::getIsDeleted, 0)
                .orderByAsc(AppointmentEntity::getCreatedAt));
        Map<LocalDate, Map<String, Object>> grouped = initDailyRange(start, end);
        for (AppointmentEntity appointment : appointments) {
            LocalDate createdDate = appointment.getCreatedAt().toLocalDate();
            Map<String, Object> stat = grouped.get(createdDate);
            stat.put("createdCount", ((Integer) stat.get("createdCount")) + 1);
            if (appointment.getStatus() != null && appointment.getStatus() == 3) {
                LocalDate cancelDate = appointment.getUpdatedAt().toLocalDate();
                Map<String, Object> cancelStat = grouped.get(cancelDate);
                if (cancelStat != null) {
                    cancelStat.put("cancelCount", ((Integer) cancelStat.get("cancelCount")) + 1);
                }
            }
        }
        return new ArrayList<Map<String, Object>>(grouped.values());
    }

    public Map<String, Object> orderConversion(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.now().minusDays(29) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        long appointmentCount = appointmentMapper.selectCount(new LambdaQueryWrapper<AppointmentEntity>()
                .ge(AppointmentEntity::getCreatedAt, start.atStartOfDay())
                .lt(AppointmentEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .eq(AppointmentEntity::getIsDeleted, 0));
        long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                .ge(OrderEntity::getCreatedAt, start.atStartOfDay())
                .lt(OrderEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .eq(OrderEntity::getIsDeleted, 0));
        long paidCount = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                .ge(OrderEntity::getPayTime, start.atStartOfDay())
                .lt(OrderEntity::getPayTime, end.plusDays(1).atStartOfDay())
                .in(OrderEntity::getStatus, 1, 2, 3, 4)
                .eq(OrderEntity::getIsDeleted, 0));
        long refundCount = refundApplyMapper.selectCount(new LambdaQueryWrapper<RefundApplyEntity>()
                .ge(RefundApplyEntity::getCreatedAt, start.atStartOfDay())
                .lt(RefundApplyEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                .eq(RefundApplyEntity::getIsDeleted, 0));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("appointmentCount", appointmentCount);
        result.put("orderCount", orderCount);
        result.put("paidCount", paidCount);
        result.put("refundCount", refundCount);
        return result;
    }

    public List<Map<String, Object>> packageAnalysis(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.now().minusDays(29) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        List<ExamPackageEntity> packages = examPackageMapper.selectList(new LambdaQueryWrapper<ExamPackageEntity>()
                .eq(ExamPackageEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageEntity::getId));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExamPackageEntity packageEntity : packages) {
            long appointmentCount = appointmentMapper.selectCount(new LambdaQueryWrapper<AppointmentEntity>()
                    .eq(AppointmentEntity::getPackageId, packageEntity.getId())
                    .ge(AppointmentEntity::getCreatedAt, start.atStartOfDay())
                    .lt(AppointmentEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                    .eq(AppointmentEntity::getIsDeleted, 0));
            long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                    .eq(OrderEntity::getPackageId, packageEntity.getId())
                    .ge(OrderEntity::getCreatedAt, start.atStartOfDay())
                    .lt(OrderEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                    .eq(OrderEntity::getIsDeleted, 0));
            List<OrderEntity> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                    .eq(OrderEntity::getPackageId, packageEntity.getId())
                    .ge(OrderEntity::getCreatedAt, start.atStartOfDay())
                    .lt(OrderEntity::getCreatedAt, end.plusDays(1).atStartOfDay())
                    .in(OrderEntity::getStatus, 1, 2, 3, 4)
                    .eq(OrderEntity::getIsDeleted, 0));
            BigDecimal paidAmount = BigDecimal.ZERO;
            for (OrderEntity order : paidOrders) {
                paidAmount = paidAmount.add(order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount());
            }
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("packageId", packageEntity.getId());
            item.put("packageCode", packageEntity.getPackageCode());
            item.put("packageName", packageEntity.getPackageName());
            item.put("appointmentCount", appointmentCount);
            item.put("orderCount", orderCount);
            item.put("paidAmount", paidAmount);
            result.add(item);
        }
        return result;
    }

    private Map<LocalDate, Map<String, Object>> initDailyRange(LocalDate start, LocalDate end) {
        Map<LocalDate, Map<String, Object>> result = new HashMap<LocalDate, Map<String, Object>>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("date", current);
            item.put("createdCount", 0);
            item.put("cancelCount", 0);
            result.put(current, item);
            current = current.plusDays(1);
        }
        return result;
    }
}
