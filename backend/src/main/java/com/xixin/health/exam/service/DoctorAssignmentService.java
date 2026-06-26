package com.xixin.health.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
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

/**
 * 医生分配服务 - 智能分配检查医生
 */
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

    /** 分配医生 */
    public Map<String, Object> assignDoctor(String departmentCode, String centerCode) {
        return assignDoctor(departmentCode, centerCode, Collections.<Long>emptySet());
    }

    /** 分配医生(可排除指定医生) */
    /**
     * 分配医生 - 优先按科室关系表过滤，再按工作量选择，兆底降级
     */
    public Map<String, Object> assignDoctor(String departmentCode, String centerCode, Set<Long> excludedDoctorIds) {
        Set<Long> normalizedExcluded = excludedDoctorIds == null
                ? Collections.<Long>emptySet()
                : new LinkedHashSet<Long>(excludedDoctorIds);

        // 策略1：按 doctor_department_rel 表按科室过滤
        if (departmentCode != null && !departmentCode.isEmpty()) {
            StaffAccountEntity doctor = selectFromDepartmentRel(departmentCode, centerCode, normalizedExcluded);
            if (doctor != null) {
                return buildResult(doctor, departmentCode, centerCode, "department_rel");
            }
        }

        // 策略2：按 staff_account.department_code 匹配
        if (departmentCode != null && !departmentCode.isEmpty()) {
            StaffAccountEntity doctor = selectFromStaffAccount(departmentCode, normalizedExcluded);
            if (doctor != null) {
                return buildResult(doctor, departmentCode, centerCode, "staff_account");
            }
        }

        // 策略3：兆底 - 全部医生中选工作量最低的
        StaffAccountEntity doctor = selectLowestWorkload(normalizedExcluded);
        if (doctor != null) {
            return buildResult(doctor, departmentCode, centerCode, "fallback");
        }

        log.warn("No available doctor for department={}, center={}", departmentCode, centerCode);
        throw new BizException(ErrorCode.DOCTOR_ASSIGN_INVALID.getCode(), "没有可分配的医生");
    }

    private StaffAccountEntity selectFromDepartmentRel(String departmentCode, String centerCode, Set<Long> excluded) {
        List<DoctorDepartmentRelEntity> rels = doctorDepartmentRelMapper.selectList(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getDepartmentCode, departmentCode)
                        .eq(centerCode != null && !centerCode.isEmpty() && !"DEFAULT".equals(centerCode),
                                DoctorDepartmentRelEntity::getCenterCode, centerCode)
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .orderByDesc(DoctorDepartmentRelEntity::getIsPrimary));

        StaffAccountEntity best = null;
        long minWorkload = Long.MAX_VALUE;
        for (DoctorDepartmentRelEntity rel : rels) {
            if (excluded.contains(rel.getDoctorId())) continue;
            StaffAccountEntity staff = staffAccountMapper.selectById(rel.getDoctorId());
            if (staff == null || staff.getStatus() == null || staff.getStatus() != 1 || staff.getIsDeleted() != 0) continue;
            if (!isDoctorAccount(staff)) continue;
            long workload = countPendingWorkload(staff.getId());
            if (workload < minWorkload) {
                minWorkload = workload;
                best = staff;
            }
        }
        return best;
    }

    private StaffAccountEntity selectFromStaffAccount(String departmentCode, Set<Long> excluded) {
        List<StaffAccountEntity> staffList = staffAccountMapper.selectList(
                new LambdaQueryWrapper<StaffAccountEntity>()
                        .eq(StaffAccountEntity::getStatus, 1)
                        .eq(StaffAccountEntity::getIsDeleted, 0)
                        .eq(StaffAccountEntity::getDepartmentCode, departmentCode));

        StaffAccountEntity best = null;
        long minWorkload = Long.MAX_VALUE;
        for (StaffAccountEntity staff : staffList) {
            if (excluded.contains(staff.getId())) continue;
            if (!isDoctorAccount(staff)) continue;
            long workload = countPendingWorkload(staff.getId());
            if (workload < minWorkload) {
                minWorkload = workload;
                best = staff;
            }
        }
        return best;
    }

    private StaffAccountEntity selectLowestWorkload(Set<Long> excluded) {
        List<StaffAccountEntity> activeStaff = staffAccountMapper.selectList(
                new LambdaQueryWrapper<StaffAccountEntity>()
                        .eq(StaffAccountEntity::getStatus, 1)
                        .eq(StaffAccountEntity::getIsDeleted, 0));

        StaffAccountEntity best = null;
        long minWorkload = Long.MAX_VALUE;
        for (StaffAccountEntity staff : activeStaff) {
            if (excluded.contains(staff.getId())) continue;
            if (!isDoctorAccount(staff)) continue;
            long workload = countPendingWorkload(staff.getId());
            if (workload < minWorkload) {
                minWorkload = workload;
                best = staff;
            }
        }
        return best;
    }

    private Map<String, Object> buildResult(StaffAccountEntity doctor, String departmentCode, String centerCode, String strategy) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("doctorId", doctor.getId());
        result.put("doctorName", doctor.getDisplayName());
        log.info("Doctor assigned: departmentCode={}, centerCode={}, doctorId={}, strategy={}",
                departmentCode, centerCode, doctor.getId(), strategy);
        return result;
    }


    /** 根据路线分配医生 */
    public Map<String, Object> assignFromRoute(ExamDepartmentRouteEntity route) {
        return assignFromRoute(route, Collections.<Long>emptySet());
    }

    public Map<String, Object> assignFromRoute(ExamDepartmentRouteEntity route, Set<Long> excludedDoctorIds) {
        String departmentCode = route != null ? route.getDepartmentCode() : "DEFAULT";
        String centerCode = route != null ? route.getCenterCode() : "DEFAULT";
        return assignDoctor(departmentCode, centerCode, excludedDoctorIds);
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
