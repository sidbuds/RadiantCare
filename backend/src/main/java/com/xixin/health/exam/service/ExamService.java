package com.xixin.health.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.appointment.entity.AppointmentEntity;
import com.xixin.health.appointment.entity.ExamPackageItemEntity;
import com.xixin.health.appointment.mapper.AppointmentMapper;
import com.xixin.health.appointment.mapper.ExamPackageItemMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.common.util.NoGenerator;
import com.xixin.health.exam.dto.ExamResultEntryDto;
import com.xixin.health.exam.dto.GenerateExamTaskRequest;
import com.xixin.health.exam.dto.GuideItemVO;
import com.xixin.health.exam.dto.SubmitExamResultRequest;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.exam.entity.ExamResultEntity;
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.entity.ExamTaskItemEntity;
import com.xixin.health.exam.mapper.ExamDepartmentRouteMapper;
import com.xixin.health.exam.mapper.ExamResultMapper;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.operator.service.OperatorPackageService;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.mapper.OrderMapper;
import com.xixin.health.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class ExamService {

    private final OrderMapper orderMapper;
    private final AppointmentMapper appointmentMapper;
    private final ExamTaskMapper examTaskMapper;
    private final ExamTaskItemMapper examTaskItemMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ExamDepartmentRouteMapper examDepartmentRouteMapper;
    private final ExamResultMapper examResultMapper;
    private final DoctorAssignmentService doctorAssignmentService;
    private final ReportService reportService;
    private final OperatorPackageService operatorPackageService;

    public ExamService(OrderMapper orderMapper,
                       AppointmentMapper appointmentMapper,
                       ExamTaskMapper examTaskMapper,
                       ExamTaskItemMapper examTaskItemMapper,
                       ExamPackageItemMapper examPackageItemMapper,
                       ExamDepartmentRouteMapper examDepartmentRouteMapper,
                       ExamResultMapper examResultMapper,
                       DoctorAssignmentService doctorAssignmentService,
                       ReportService reportService,
                       OperatorPackageService operatorPackageService) {
        this.orderMapper = orderMapper;
        this.appointmentMapper = appointmentMapper;
        this.examTaskMapper = examTaskMapper;
        this.examTaskItemMapper = examTaskItemMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.examDepartmentRouteMapper = examDepartmentRouteMapper;
        this.examResultMapper = examResultMapper;
        this.doctorAssignmentService = doctorAssignmentService;
        this.reportService = reportService;
        this.operatorPackageService = operatorPackageService;
    }

    @Transactional
    public Map<String, Object> generateTask(GenerateExamTaskRequest request) {
        return generateTaskForPaidOrder(getOrderByNo(request.getOrderNo()));
    }

    @Transactional
    public Map<String, Object> generateTaskForPaidOrder(OrderEntity order) {
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "订单未支付，不能生成体检任务");
        }
        ExamTaskEntity existing = examTaskMapper.selectOne(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getOrderId, order.getId())
                .eq(ExamTaskEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (existing != null) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("taskNo", existing.getTaskNo());
            result.put("appointmentNo", existing.getAppointmentNo());
            result.put("packageId", existing.getPackageId());
            result.put("status", "WAIT_EXAM");
            result.put("isExisting", true);
            return result;
        }

        AppointmentEntity appointment = appointmentMapper.selectById(order.getAppointmentId());
        if (appointment == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "预约不存在");
        }
        if (!operatorPackageService.isPackageAvailableAtCenter(order.getPackageId(), appointment.getCenterCode())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "套餐不适用于预约体检中心");
        }
        List<ExamPackageItemEntity> packageItems = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, order.getPackageId())
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageItemEntity::getSortNo));
        if (packageItems.isEmpty()) {
            throw new BizException(ErrorCode.PACKAGE_ITEM_REQUIRED);
        }
        Map<String, ExamDepartmentRouteEntity> routeByItemCode =
                loadAndValidateRoutes(appointment.getCenterCode(), order.getPackageId(), packageItems);

        ExamTaskEntity task = new ExamTaskEntity();
        task.setTaskNo(NoGenerator.next("ET"));
        task.setAppointmentId(appointment.getId());
        task.setAppointmentNo(appointment.getAppointmentNo());
        task.setOrderId(order.getId());
        task.setOrderNo(order.getOrderNo());
        task.setUserId(order.getUserId());
        task.setPackageId(order.getPackageId());
        task.setCenterCode(appointment.getCenterCode());
        task.setTaskDate(appointment.getAppointDate());
        task.setTaskStatus(0);
        task.setReportStatus(0);
        task.setGuideStatus(0);
        task.setIsDeleted(0);
        examTaskMapper.insert(task);

        int sort = 1;
        Set<Long> assignedDoctorIds = new LinkedHashSet<Long>();
        for (ExamPackageItemEntity packageItem : packageItems) {
            ExamDepartmentRouteEntity route = routeByItemCode.get(packageItem.getItemCode());
            Map<String, Object> doctorAssignment = doctorAssignmentService.assignFromRoute(route, assignedDoctorIds);
            Long assignedDoctorId = ((Number) doctorAssignment.get("doctorId")).longValue();
            assignedDoctorIds.add(assignedDoctorId);

            ExamTaskItemEntity taskItem = new ExamTaskItemEntity();
            taskItem.setTaskItemNo(NoGenerator.next("ETI"));
            taskItem.setTaskId(task.getId());
            taskItem.setTaskNo(task.getTaskNo());
            taskItem.setItemCode(packageItem.getItemCode());
            taskItem.setItemName(packageItem.getItemName());
            taskItem.setDepartmentCode(route.getDepartmentCode());
            taskItem.setDepartmentName(route.getDepartmentName());
            taskItem.setDoctorId(assignedDoctorId);
            taskItem.setDoctorName((String) doctorAssignment.get("doctorName"));
            taskItem.setRoomNo(route.getRoomNo());
            taskItem.setRouteSort(route.getRouteSort() == null ? sort : route.getRouteSort());
            taskItem.setItemStatus(0);
            taskItem.setEntryStatus(0);
            taskItem.setIsDeleted(0);
            examTaskItemMapper.insert(taskItem);
            sort++;
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("taskNo", task.getTaskNo());
        result.put("appointmentNo", task.getAppointmentNo());
        result.put("packageId", task.getPackageId());
        result.put("itemCount", packageItems.size());
        result.put("status", "WAIT_EXAM");
        return result;
    }

    public ExamTaskEntity currentTask() {
        ExamTaskEntity task = examTaskMapper.selectOne(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getUserId, AuthContext.getUserId())
                .eq(ExamTaskEntity::getIsDeleted, 0)
                .orderByDesc(ExamTaskEntity::getId)
                .last("limit 1"));
        if (task == null) {
            throw new BizException(ErrorCode.EXAM_TASK_NOT_FOUND);
        }
        return task;
    }

    public ExamTaskEntity detail(String taskNo) {
        ExamTaskEntity task = getTaskByNo(taskNo);
        if (!task.getUserId().equals(AuthContext.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return task;
    }

    public List<ExamTaskItemEntity> taskItemsForUser(String taskNo) {
        ExamTaskEntity task = detail(taskNo);
        return examTaskItemMapper.selectList(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getTaskId, task.getId())
                .eq(ExamTaskItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamTaskItemEntity::getRouteSort));
    }

    public List<GuideItemVO> guide(String taskNo) {
        ExamTaskEntity task = detail(taskNo);
        List<ExamTaskItemEntity> items = taskItemsForUser(taskNo);
        List<ExamDepartmentRouteEntity> routes = examDepartmentRouteMapper.selectList(
                new LambdaQueryWrapper<ExamDepartmentRouteEntity>()
                        .eq(ExamDepartmentRouteEntity::getCenterCode, task.getCenterCode())
                        .eq(ExamDepartmentRouteEntity::getPackageId, task.getPackageId())
                        .eq(ExamDepartmentRouteEntity::getStatus, 1)
                        .eq(ExamDepartmentRouteEntity::getIsDeleted, 0)
                        .orderByAsc(ExamDepartmentRouteEntity::getRouteSort));

        Map<String, ExamDepartmentRouteEntity> routeMap = new HashMap<String, ExamDepartmentRouteEntity>();
        for (ExamDepartmentRouteEntity route : routes) {
            routeMap.put(route.getItemCode(), route);
        }
        List<GuideItemVO> result = new ArrayList<GuideItemVO>();
        for (ExamTaskItemEntity item : items) {
            GuideItemVO vo = new GuideItemVO();
            vo.setTaskItemId(item.getId());
            vo.setTaskItemNo(item.getTaskItemNo());
            vo.setItemCode(item.getItemCode());
            vo.setItemName(item.getItemName());
            vo.setDoctorName(item.getDoctorName());
            vo.setItemStatus(item.getItemStatus());
            ExamDepartmentRouteEntity route = routeMap.get(item.getItemCode());
            if (route != null) {
                vo.setDepartmentCode(route.getDepartmentCode());
                vo.setDepartmentName(route.getDepartmentName());
                vo.setRoomNo(route.getRoomNo());
                vo.setFloorNo(route.getFloorNo());
                vo.setBuildingNo(route.getBuildingNo());
                vo.setGuideText(route.getGuideText());
                vo.setNeedEmptyStomach(route.getNeedEmptyStomach());
                vo.setRouteSort(route.getRouteSort());
            } else {
                vo.setDepartmentCode(item.getDepartmentCode());
                vo.setDepartmentName(item.getDepartmentName());
                vo.setRoomNo(item.getRoomNo());
                vo.setRouteSort(item.getRouteSort());
            }
            result.add(vo);
        }
        result.sort((left, right) -> {
            Integer leftSort = left.getRouteSort() == null ? Integer.MAX_VALUE : left.getRouteSort();
            Integer rightSort = right.getRouteSort() == null ? Integer.MAX_VALUE : right.getRouteSort();
            return leftSort.compareTo(rightSort);
        });
        return result;
    }

    public List<ExamTaskItemEntity> doctorTodo() {
        return examTaskItemMapper.selectList(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getDoctorId, AuthContext.getAccountId())
                .eq(ExamTaskItemEntity::getIsDeleted, 0)
                .in(ExamTaskItemEntity::getItemStatus, 0, 1, 4)
                .orderByAsc(ExamTaskItemEntity::getId));
    }

    @Transactional
    public int revokeForRefundedOrder(Long orderId, String reason) {
        if (orderId == null) {
            return 0;
        }
        List<ExamTaskEntity> tasks = examTaskMapper.selectList(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getOrderId, orderId)
                .eq(ExamTaskEntity::getIsDeleted, 0));
        int count = 0;
        for (ExamTaskEntity task : tasks) {
            examTaskItemMapper.update(null, new LambdaUpdateWrapper<ExamTaskItemEntity>()
                    .eq(ExamTaskItemEntity::getTaskId, task.getId())
                    .eq(ExamTaskItemEntity::getIsDeleted, 0)
                    .ne(ExamTaskItemEntity::getItemStatus, 2)
                    .set(ExamTaskItemEntity::getItemStatus, 5)
                    .set(ExamTaskItemEntity::getSkipReason, reason)
                    .set(ExamTaskItemEntity::getIsDeleted, 1)
                    .set(ExamTaskItemEntity::getUpdatedAt, LocalDateTime.now()));
            int updated = examTaskMapper.update(null, new LambdaUpdateWrapper<ExamTaskEntity>()
                    .eq(ExamTaskEntity::getId, task.getId())
                    .eq(ExamTaskEntity::getIsDeleted, 0)
                    .set(ExamTaskEntity::getTaskStatus, 5)
                    .set(ExamTaskEntity::getRemark, reason)
                    .set(ExamTaskEntity::getIsDeleted, 1)
                    .set(ExamTaskEntity::getUpdatedAt, LocalDateTime.now()));
            if (updated > 0) {
                count++;
            }
        }
        return count;
    }

    public Map<String, Object> doctorItemDetail(String taskNo, String taskItemNo) {
        ExamTaskItemEntity item = getDoctorTaskItem(taskNo, taskItemNo);
        ExamTaskEntity task = getTaskByNo(taskNo);
        ExamPackageItemEntity packageItem = examPackageItemMapper.selectOne(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, task.getPackageId())
                .eq(ExamPackageItemEntity::getItemCode, item.getItemCode())
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .last("limit 1"));
        Map<String, Object> preset = new HashMap<String, Object>();
        preset.put("metricCode", packageItem == null ? item.getItemCode() : packageItem.getItemCode());
        preset.put("metricName", packageItem == null ? item.getItemName() : packageItem.getItemName());
        preset.put("unit", packageItem == null ? null : packageItem.getUnit());
        preset.put("refRange", packageItem == null ? null : packageItem.getRefRange());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("item", item);
        result.put("presetMetrics", Collections.singletonList(preset));
        return result;
    }

    @Transactional
    public void start(String taskNo, String taskItemNo) {
        ExamTaskItemEntity item = getDoctorTaskItem(taskNo, taskItemNo);
        examTaskItemMapper.update(null, new LambdaUpdateWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getId, item.getId())
                .set(ExamTaskItemEntity::getItemStatus, 1)
                .set(ExamTaskItemEntity::getStartTime, LocalDateTime.now()));
        examTaskMapper.update(null, new LambdaUpdateWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getId, item.getTaskId())
                .set(ExamTaskEntity::getTaskStatus, 1)
                .set(ExamTaskEntity::getGuideStatus, 1)
                .set(ExamTaskEntity::getStartTime, LocalDateTime.now()));
    }

    @Transactional
    public Map<String, Object> submitResult(String taskNo, String taskItemNo, SubmitExamResultRequest request) {
        ExamTaskItemEntity item = getDoctorTaskItem(taskNo, taskItemNo);
        ExamTaskEntity task = getTaskByNo(taskNo);
        for (ExamResultEntryDto entry : request.getResultEntries()) {
            ExamResultEntity result = new ExamResultEntity();
            result.setResultNo(NoGenerator.next("ER"));
            result.setTaskId(item.getTaskId());
            result.setTaskItemId(item.getId());
            result.setTaskItemNo(item.getTaskItemNo());
            result.setUserId(task.getUserId());
            result.setItemCode(entry.getMetricCode());
            result.setItemName(entry.getMetricName());
            result.setResultType(request.getItemCode());
            result.setResultValue(entry.getResultValue());
            result.setResultNumber(entry.getResultNumber());
            result.setUnit(entry.getUnit());
            result.setRefRange(entry.getRefRange());
            result.setConclusion(request.getConclusion());
            result.setIsAbnormal(Boolean.TRUE.equals(entry.getAbnormal()) ? 1 : 0);
            result.setAbnormalLevel(entry.getAbnormalLevel() == null ? 0 : entry.getAbnormalLevel());
            result.setEntryDoctorId(AuthContext.getAccountId());
            result.setEntryDoctorName(AuthContext.get() == null ? null : AuthContext.get().getUsername());
            result.setEntryTime(LocalDateTime.now());
            result.setAuditStatus(0);
            result.setIsDeleted(0);
            examResultMapper.insert(result);
        }
        examTaskItemMapper.update(null, new LambdaUpdateWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getId, item.getId())
                .set(ExamTaskItemEntity::getEntryStatus, 2));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("taskItemNo", taskItemNo);
        result.put("entryStatus", "SAVED");
        return result;
    }

    @Transactional
    public void completeItem(String taskNo, String taskItemNo) {
        ExamTaskItemEntity item = getDoctorTaskItem(taskNo, taskItemNo);
        long resultCount = examResultMapper.selectCount(new LambdaQueryWrapper<ExamResultEntity>()
                .eq(ExamResultEntity::getTaskItemId, item.getId())
                .eq(ExamResultEntity::getIsDeleted, 0));
        if (resultCount == 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "请先录入检查结果");
        }
        examTaskItemMapper.update(null, new LambdaUpdateWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getId, item.getId())
                .set(ExamTaskItemEntity::getItemStatus, 2)
                .set(ExamTaskItemEntity::getCompleteTime, LocalDateTime.now()));
        long remain = examTaskItemMapper.selectCount(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getTaskId, item.getTaskId())
                .eq(ExamTaskItemEntity::getIsDeleted, 0)
                .ne(ExamTaskItemEntity::getItemStatus, 2));
        if (remain == 0) {
            examTaskMapper.update(null, new LambdaUpdateWrapper<ExamTaskEntity>()
                    .eq(ExamTaskEntity::getId, item.getTaskId())
                    .set(ExamTaskEntity::getTaskStatus, 2)
                    .set(ExamTaskEntity::getGuideStatus, 2)
                    .set(ExamTaskEntity::getCompleteTime, LocalDateTime.now()));
            triggerReportGenerationAfterCommit(taskNo);
        }
    }

    public ExamTaskEntity getTaskByNo(String taskNo) {
        ExamTaskEntity task = examTaskMapper.selectOne(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getTaskNo, taskNo)
                .eq(ExamTaskEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (task == null) {
            throw new BizException(ErrorCode.EXAM_TASK_NOT_FOUND);
        }
        return task;
    }

    private Map<String, ExamDepartmentRouteEntity> loadAndValidateRoutes(String centerCode,
                                                                         Long packageId,
                                                                         List<ExamPackageItemEntity> packageItems) {
        List<ExamDepartmentRouteEntity> routes = examDepartmentRouteMapper.selectList(new LambdaQueryWrapper<ExamDepartmentRouteEntity>()
                .eq(ExamDepartmentRouteEntity::getCenterCode, centerCode)
                .eq(ExamDepartmentRouteEntity::getPackageId, packageId)
                .eq(ExamDepartmentRouteEntity::getStatus, 1)
                .eq(ExamDepartmentRouteEntity::getIsDeleted, 0)
                .orderByAsc(ExamDepartmentRouteEntity::getRouteSort));
        Map<String, ExamDepartmentRouteEntity> routeByItemCode = new HashMap<String, ExamDepartmentRouteEntity>();
        for (ExamDepartmentRouteEntity route : routes) {
            routeByItemCode.put(route.getItemCode(), route);
        }
        List<String> missing = new ArrayList<String>();
        for (ExamPackageItemEntity packageItem : packageItems) {
            if (!routeByItemCode.containsKey(packageItem.getItemCode())) {
                missing.add(packageItem.getItemCode());
            }
        }
        if (!missing.isEmpty()) {
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(),
                    "该中心套餐导引路线未配置完整：" + centerCode + " 缺少 " + missing);
        }
        return routeByItemCode;
    }

    private OrderEntity getOrderByNo(String orderNo) {
        OrderEntity order = orderMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderNo, orderNo)
                .eq(OrderEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    private void triggerReportGenerationAfterCommit(final String taskNo) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    autoGenerateReport(taskNo);
                }
            });
            return;
        }
        autoGenerateReport(taskNo);
    }

    private void autoGenerateReport(String taskNo) {
        try {
            reportService.autoGenerateForCompletedTask(taskNo);
        } catch (Exception exception) {
            log.error("Auto generate report failed: taskNo={}", taskNo, exception);
        }
    }

    private ExamTaskItemEntity getDoctorTaskItem(String taskNo, String taskItemNo) {
        ExamTaskEntity task = getTaskByNo(taskNo);
        ExamTaskItemEntity item = examTaskItemMapper.selectOne(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getTaskId, task.getId())
                .eq(ExamTaskItemEntity::getTaskItemNo, taskItemNo)
                .eq(ExamTaskItemEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (item == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "检查项不存在");
        }
        if (!AuthContext.getAccountId().equals(item.getDoctorId())) {
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID);
        }
        return item;
    }
}
