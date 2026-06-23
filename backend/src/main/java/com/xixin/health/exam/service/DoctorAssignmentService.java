package com.xixin.health.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.exam.entity.ExamTaskItemEntity;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DoctorAssignmentService {

    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final ExamTaskItemMapper examTaskItemMapper;

    public DoctorAssignmentService(StaffAccountMapper staffAccountMapper,
                                   StaffRoleRelMapper staffRoleRelMapper,
                                   ExamTaskItemMapper examTaskItemMapper) {
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.examTaskItemMapper = examTaskItemMapper;
    }

    public Map<String, Object> assignDoctor(String departmentCode, String centerCode) {
        List<StaffAccountEntity> activeStaff = staffAccountMapper.selectList(
                new LambdaQueryWrapper<StaffAccountEntity>()
                        .eq(StaffAccountEntity::getStatus, 1)
                        .eq(StaffAccountEntity::getIsDeleted, 0));

        StaffAccountEntity selectedDoctor = null;
        long minWorkload = Long.MAX_VALUE;
        for (StaffAccountEntity staff : activeStaff) {
            if (!isDoctorAccount(staff)) {
                continue;
            }
            long workload = countPendingWorkload(staff.getId());
            if (workload < minWorkload) {
                minWorkload = workload;
                selectedDoctor = staff;
            }
        }

        if (selectedDoctor == null) {
            log.warn("No available doctor for department={}, center={}", departmentCode, centerCode);
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(), "没有可分配的医生");
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("doctorId", selectedDoctor.getId());
        result.put("doctorName", selectedDoctor.getDisplayName());
        log.info("Doctor assigned: departmentCode={}, centerCode={}, doctorId={}, workload={}",
                departmentCode, centerCode, selectedDoctor.getId(), minWorkload);
        return result;
    }

    public Map<String, Object> assignFromRoute(ExamDepartmentRouteEntity route) {
        String departmentCode = route != null ? route.getDepartmentCode() : "DEFAULT";
        String centerCode = route != null ? route.getCenterCode() : "DEFAULT";
        return assignDoctor(departmentCode, centerCode);
    }

    private boolean isDoctorAccount(StaffAccountEntity account) {
        Long count = staffRoleRelMapper.selectCount(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, account.getId())
                .eq(StaffRoleRelEntity::getRoleCode, "DOCTOR")
                .eq(StaffRoleRelEntity::getIsDeleted, 0));
        return count != null && count > 0;
    }

    private long countPendingWorkload(Long doctorId) {
        Long count = examTaskItemMapper.selectCount(
                new LambdaQueryWrapper<ExamTaskItemEntity>()
                        .eq(ExamTaskItemEntity::getDoctorId, doctorId)
                        .eq(ExamTaskItemEntity::getIsDeleted, 0)
                        .in(ExamTaskItemEntity::getItemStatus, 0, 1));
        return count == null ? 0L : count;
    }
}
