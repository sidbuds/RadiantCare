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
import com.xixin.health.exam.dto.GenerateExamTaskRequest;
import com.xixin.health.exam.dto.SubmitExamResultRequest;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.exam.entity.ExamResultEntity;
import com.xixin.health.exam.entity.ExamTaskEntity;
import com.xixin.health.exam.entity.ExamTaskItemEntity;
import com.xixin.health.exam.mapper.ExamDepartmentRouteMapper;
import com.xixin.health.exam.mapper.ExamResultMapper;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import com.xixin.health.exam.mapper.ExamTaskMapper;
import com.xixin.health.order.entity.OrderEntity;
import com.xixin.health.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamService {

    private final OrderService orderService;
    private final AppointmentMapper appointmentMapper;
    private final ExamTaskMapper examTaskMapper;
    private final ExamTaskItemMapper examTaskItemMapper;
    private final ExamPackageItemMapper examPackageItemMapper;
    private final ExamDepartmentRouteMapper examDepartmentRouteMapper;
    private final ExamResultMapper examResultMapper;

    public ExamService(OrderService orderService,
                       AppointmentMapper appointmentMapper,
                       ExamTaskMapper examTaskMapper,
                       ExamTaskItemMapper examTaskItemMapper,
                       ExamPackageItemMapper examPackageItemMapper,
                       ExamDepartmentRouteMapper examDepartmentRouteMapper,
                       ExamResultMapper examResultMapper) {
        this.orderService = orderService;
        this.appointmentMapper = appointmentMapper;
        this.examTaskMapper = examTaskMapper;
        this.examTaskItemMapper = examTaskItemMapper;
        this.examPackageItemMapper = examPackageItemMapper;
        this.examDepartmentRouteMapper = examDepartmentRouteMapper;
        this.examResultMapper = examResultMapper;
    }

    @Transactional
    public Map<String, Object> generateTask(GenerateExamTaskRequest request) {
        OrderEntity order = orderService.getByNo(request.getOrderNo());
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "订单未支付，不能生成体检任务");
        }
        long taskCount = examTaskMapper.selectCount(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getOrderId, order.getId())
                .eq(ExamTaskEntity::getIsDeleted, 0));
        if (taskCount > 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该订单已生成体检任务");
        }
        AppointmentEntity appointment = appointmentMapper.selectById(order.getAppointmentId());
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

        List<ExamPackageItemEntity> packageItems = examPackageItemMapper.selectList(new LambdaQueryWrapper<ExamPackageItemEntity>()
                .eq(ExamPackageItemEntity::getPackageId, order.getPackageId())
                .eq(ExamPackageItemEntity::getIsDeleted, 0)
                .orderByAsc(ExamPackageItemEntity::getSortNo));
        int sort = 1;
        for (ExamPackageItemEntity packageItem : packageItems) {
            ExamDepartmentRouteEntity route = examDepartmentRouteMapper.selectOne(new LambdaQueryWrapper<ExamDepartmentRouteEntity>()
                    .eq(ExamDepartmentRouteEntity::getCenterCode, appointment.getCenterCode())
                    .eq(ExamDepartmentRouteEntity::getPackageId, order.getPackageId())
                    .eq(ExamDepartmentRouteEntity::getItemCode, packageItem.getItemCode())
                    .eq(ExamDepartmentRouteEntity::getStatus, 1)
                    .eq(ExamDepartmentRouteEntity::getIsDeleted, 0));

            ExamTaskItemEntity taskItem = new ExamTaskItemEntity();
            taskItem.setTaskItemNo(NoGenerator.next("ETI"));
            taskItem.setTaskId(task.getId());
            taskItem.setTaskNo(task.getTaskNo());
            taskItem.setItemCode(packageItem.getItemCode());
            taskItem.setItemName(packageItem.getItemName());
            taskItem.setDepartmentCode(route == null ? "DEFAULT" : route.getDepartmentCode());
            taskItem.setDepartmentName(route == null ? "默认科室" : route.getDepartmentName());
            taskItem.setDoctorId(2L);
            taskItem.setDoctorName("doctor");
            taskItem.setRoomNo(route == null ? "待分配" : route.getRoomNo());
            taskItem.setRouteSort(route == null ? sort : route.getRouteSort());
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

    public List<ExamTaskItemEntity> guide(String taskNo) {
        return taskItemsForUser(taskNo);
    }

    public List<ExamTaskItemEntity> doctorTodo() {
        return examTaskItemMapper.selectList(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getDoctorId, AuthContext.getUserId())
                .eq(ExamTaskItemEntity::getIsDeleted, 0)
                .in(ExamTaskItemEntity::getItemStatus, 0, 1, 4)
                .orderByAsc(ExamTaskItemEntity::getId));
    }

    public ExamTaskItemEntity doctorItemDetail(String taskNo, String taskItemNo) {
        ExamTaskItemEntity item = getDoctorTaskItem(taskNo, taskItemNo);
        if (!AuthContext.getUserId().equals(item.getDoctorId())) {
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID);
        }
        return item;
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
        for (com.xixin.health.exam.dto.ExamResultEntryDto entry : request.getResultEntries()) {
            ExamResultEntity result = new ExamResultEntity();
            result.setResultNo(NoGenerator.next("ER"));
            result.setTaskId(item.getTaskId());
            result.setTaskItemId(item.getId());
            result.setTaskItemNo(item.getTaskItemNo());
            result.setUserId(getTaskByNo(taskNo).getUserId());
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
            result.setEntryDoctorId(AuthContext.getUserId());
            result.setEntryDoctorName(AuthContext.get().getUsername());
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
        }
    }

    public ExamTaskEntity getTaskByNo(String taskNo) {
        ExamTaskEntity task = examTaskMapper.selectOne(new LambdaQueryWrapper<ExamTaskEntity>()
                .eq(ExamTaskEntity::getTaskNo, taskNo)
                .eq(ExamTaskEntity::getIsDeleted, 0));
        if (task == null) {
            throw new BizException(ErrorCode.EXAM_TASK_NOT_FOUND);
        }
        return task;
    }

    private ExamTaskItemEntity getDoctorTaskItem(String taskNo, String taskItemNo) {
        ExamTaskEntity task = getTaskByNo(taskNo);
        ExamTaskItemEntity item = examTaskItemMapper.selectOne(new LambdaQueryWrapper<ExamTaskItemEntity>()
                .eq(ExamTaskItemEntity::getTaskId, task.getId())
                .eq(ExamTaskItemEntity::getTaskItemNo, taskItemNo)
                .eq(ExamTaskItemEntity::getIsDeleted, 0));
        if (item == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "检查项不存在");
        }
        if (!AuthContext.getUserId().equals(item.getDoctorId())) {
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID);
        }
        return item;
    }
}
