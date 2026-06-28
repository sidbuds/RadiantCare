package com.xixin.health.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.exam.entity.ExamDepartmentRouteEntity;
import com.xixin.health.exam.entity.ExamTaskItemEntity;
import com.xixin.health.exam.mapper.ExamTaskItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class DoctorAssignmentService {

    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final ExamTaskItemMapper examTaskItemMapper;
    private final DoctorDepartmentRelMapper doctorDepartmentRelMapper;

    public DoctorAssignmentService(StaffAccountMapper staffAccountMapper,
                                   StaffRoleRelMapper staffRoleRelMapper,
                                   ExamTaskItemMapper examTaskItemMapper,
                                   DoctorDepartmentRelMapper doctorDepartmentRelMapper) {
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.examTaskItemMapper = examTaskItemMapper;
        this.doctorDepartmentRelMapper = doctorDepartmentRelMapper;
    }

    public Map<String, Object> assignDoctor(String departmentCode, String centerCode) {
        return assignDoctor(departmentCode, centerCode, Collections.<Long>emptySet());
    }

    public Map<String, Object> assignDoctor(String departmentCode, String centerCode, Set<Long> excludedDoctorIds) {
        if (isBlank(centerCode) || isBlank(departmentCode)) {
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(), "体检中心和科室不能为空");
        }
        Set<Long> excluded = excludedDoctorIds == null
                ? Collections.<Long>emptySet()
                : new LinkedHashSet<Long>(excludedDoctorIds);

        List<DoctorDepartmentRelEntity> rels = doctorDepartmentRelMapper.selectList(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getCenterCode, centerCode)
                        .eq(DoctorDepartmentRelEntity::getDepartmentCode, departmentCode)
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .orderByDesc(DoctorDepartmentRelEntity::getIsPrimary));

        StaffAccountEntity best = null;
        long minWorkload = Long.MAX_VALUE;
        for (DoctorDepartmentRelEntity rel : rels) {
            if (excluded.contains(rel.getDoctorId())) {
                continue;
            }
            StaffAccountEntity staff = staffAccountMapper.selectById(rel.getDoctorId());
            if (staff == null || !Integer.valueOf(1).equals(staff.getStatus()) || !Integer.valueOf(0).equals(staff.getIsDeleted())) {
                continue;
            }
            if (!isDoctorAccount(staff.getId())) {
                continue;
            }
            long workload = countPendingWorkload(staff.getId());
            if (workload < minWorkload) {
                minWorkload = workload;
                best = staff;
            }
        }

        if (best == null) {
            log.warn("No doctor matched center={}, department={}", centerCode, departmentCode);
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(),
                    "未配置可用医生：" + centerCode + "/" + departmentCode);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("doctorId", best.getId());
        result.put("doctorName", best.getDisplayName());
        return result;
    }

    public Map<String, Object> assignFromRoute(ExamDepartmentRouteEntity route) {
        return assignFromRoute(route, Collections.<Long>emptySet());
    }

    public Map<String, Object> assignFromRoute(ExamDepartmentRouteEntity route, Set<Long> excludedDoctorIds) {
        if (route == null) {
            throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(), "导引路线不能为空");
        }
        return assignDoctor(route.getDepartmentCode(), route.getCenterCode(), excludedDoctorIds);
    }

    private boolean isDoctorAccount(Long accountId) {
        Long count = staffRoleRelMapper.selectCount(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, accountId)
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
