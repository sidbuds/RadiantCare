package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.AuditLogService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDoctorService {

    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final DoctorDepartmentRelMapper doctorDepartmentRelMapper;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminDoctorService(StaffAccountMapper staffAccountMapper,
                              StaffRoleRelMapper staffRoleRelMapper,
                              DoctorDepartmentRelMapper doctorDepartmentRelMapper,
                              AuditLogService auditLogService) {
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.doctorDepartmentRelMapper = doctorDepartmentRelMapper;
        this.auditLogService = auditLogService;
    }

    public Map<String, Object> listDoctors(String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<StaffAccountEntity> wrapper = new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getIsDeleted, 0)
                .orderByDesc(StaffAccountEntity::getId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(StaffAccountEntity::getUsername, keyword)
                    .or().like(StaffAccountEntity::getDisplayName, keyword));
        }
        List<StaffAccountEntity> all = staffAccountMapper.selectList(wrapper);
        List<Map<String, Object>> doctors = new ArrayList<Map<String, Object>>();
        for (StaffAccountEntity account : all) {
            if (!isDoctorAccount(account.getId())) {
                continue;
            }
            List<DoctorDepartmentRelEntity> rels = activeRelations(account.getId());
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", account.getId());
            item.put("username", account.getUsername());
            item.put("displayName", account.getDisplayName());
            item.put("departmentCode", account.getDepartmentCode());
            item.put("departmentName", account.getDepartmentName());
            item.put("specialty", account.getSpecialty());
            item.put("centerCode", account.getCenterCode());
            item.put("status", account.getStatus());
            item.put("createdAt", account.getCreatedAt());
            if (!rels.isEmpty()) {
                DoctorDepartmentRelEntity primary = rels.get(0);
                item.put("centerCode", primary.getCenterCode());
                item.put("departmentCode", primary.getDepartmentCode());
                item.put("departmentName", primary.getDepartmentName());
            }
            item.put("departments", rels);
            doctors.add(item);
        }
        int total = doctors.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", doctors.subList(from, to));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Transactional
    public void updateDoctor(Long id, Map<String, Object> data) {
        StaffAccountEntity account = staffAccountMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "医生不存在");
        }

        String requestedCenterCode = data.containsKey("centerCode")
                ? normalizeNullable((String) data.get("centerCode"))
                : account.getCenterCode();
        String requestedDepartmentCode = data.containsKey("departmentCode")
                ? normalizeNullable((String) data.get("departmentCode"))
                : account.getDepartmentCode();
        String requestedDepartmentName = data.containsKey("departmentName")
                ? normalizeNullable((String) data.get("departmentName"))
                : account.getDepartmentName();

        boolean syncPrimary = data.containsKey("centerCode") || data.containsKey("departmentCode");
        if (syncPrimary) {
            if (!hasText(requestedCenterCode) || !hasText(requestedDepartmentCode)) {
                throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "医生默认中心和科室必须同时选择");
            }
            ensureSingleCenter(id, requestedCenterCode);
        }

        if (data.containsKey("displayName")) account.setDisplayName((String) data.get("displayName"));
        if (data.containsKey("departmentCode")) account.setDepartmentCode(requestedDepartmentCode);
        if (data.containsKey("departmentName")) account.setDepartmentName(requestedDepartmentName);
        if (data.containsKey("specialty")) account.setSpecialty((String) data.get("specialty"));
        if (data.containsKey("centerCode")) account.setCenterCode(requestedCenterCode);
        if (data.containsKey("status")) account.setStatus((Integer) data.get("status"));

        if (syncPrimary) {
            saveOrRestoreBinding(id, requestedCenterCode, requestedDepartmentCode, requestedDepartmentName, true);
        }
        staffAccountMapper.updateById(account);
        auditLogService.record("DOCTOR", "UPDATE", "STAFF_ACCOUNT", id);
    }

    @Transactional
    public void bindDepartment(Long doctorId, String departmentCode, String departmentName, String centerCode, Boolean isPrimary) {
        String normalizedCenterCode = normalizeRequired(centerCode, "医生科室绑定必须选择体检中心");
        String normalizedDepartmentCode = normalizeRequired(departmentCode, "医生科室绑定必须选择科室");
        String normalizedDepartmentName = normalizeNullable(departmentName);
        ensureSingleCenter(doctorId, normalizedCenterCode);

        DoctorDepartmentRelEntity existing = doctorDepartmentRelMapper.selectOne(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getDoctorId, doctorId)
                        .eq(DoctorDepartmentRelEntity::getDepartmentCode, normalizedDepartmentCode)
                        .eq(DoctorDepartmentRelEntity::getCenterCode, normalizedCenterCode)
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (existing != null) {
            throwDuplicateDepartment();
        }

        try {
            boolean primary = Boolean.TRUE.equals(isPrimary) || activeRelations(doctorId).isEmpty();
            saveOrRestoreBinding(doctorId, normalizedCenterCode, normalizedDepartmentCode, normalizedDepartmentName, primary);
            syncAccountPrimaryIfNeeded(doctorId, normalizedCenterCode, normalizedDepartmentCode, normalizedDepartmentName, primary);
            auditLogService.record("DOCTOR", "BIND", "DOCTOR_DEPARTMENT", doctorId);
        } catch (DuplicateKeyException ex) {
            throwDuplicateDepartment();
        }
    }

    @Transactional
    public void unbindDepartment(Long relId) {
        doctorDepartmentRelMapper.update(null, new LambdaUpdateWrapper<DoctorDepartmentRelEntity>()
                .eq(DoctorDepartmentRelEntity::getId, relId)
                .set(DoctorDepartmentRelEntity::getIsDeleted, 1));
        auditLogService.record("DOCTOR", "UNBIND", "DOCTOR_DEPARTMENT", relId);
    }

    private boolean isDoctorAccount(Long accountId) {
        Long count = staffRoleRelMapper.selectCount(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, accountId)
                .eq(StaffRoleRelEntity::getRoleCode, "DOCTOR")
                .eq(StaffRoleRelEntity::getIsDeleted, 0));
        return count != null && count > 0;
    }

    private List<DoctorDepartmentRelEntity> activeRelations(Long doctorId) {
        return doctorDepartmentRelMapper.selectList(new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                .eq(DoctorDepartmentRelEntity::getDoctorId, doctorId)
                .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                .orderByDesc(DoctorDepartmentRelEntity::getIsPrimary)
                .orderByAsc(DoctorDepartmentRelEntity::getCenterCode)
                .orderByAsc(DoctorDepartmentRelEntity::getDepartmentCode));
    }

    private void ensureSingleCenter(Long doctorId, String centerCode) {
        List<DoctorDepartmentRelEntity> activeRels = activeRelations(doctorId);
        for (DoctorDepartmentRelEntity rel : activeRels) {
            if (!centerCode.equals(rel.getCenterCode())) {
                throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(),
                        "医生已绑定中心 " + rel.getCenterCode() + "，不能绑定其他中心");
            }
        }
    }

    private DoctorDepartmentRelEntity saveOrRestoreBinding(Long doctorId, String centerCode, String departmentCode,
                                                           String departmentName, boolean primary) {
        DoctorDepartmentRelEntity deleted = doctorDepartmentRelMapper.selectOne(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getDoctorId, doctorId)
                        .eq(DoctorDepartmentRelEntity::getDepartmentCode, departmentCode)
                        .eq(DoctorDepartmentRelEntity::getCenterCode, centerCode)
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 1)
                        .last("limit 1"));
        if (deleted != null) {
            deleted.setDepartmentName(departmentName);
            deleted.setIsPrimary(primary ? 1 : 0);
            deleted.setIsDeleted(0);
            doctorDepartmentRelMapper.updateById(deleted);
            if (primary) {
                clearOtherPrimary(doctorId, deleted.getId());
            }
            return deleted;
        }

        DoctorDepartmentRelEntity rel = new DoctorDepartmentRelEntity();
        rel.setDoctorId(doctorId);
        rel.setDepartmentCode(departmentCode);
        rel.setDepartmentName(departmentName);
        rel.setCenterCode(centerCode);
        rel.setIsPrimary(primary ? 1 : 0);
        rel.setIsDeleted(0);
        doctorDepartmentRelMapper.insert(rel);
        if (primary) {
            clearOtherPrimary(doctorId, rel.getId());
        }
        return rel;
    }

    private void clearOtherPrimary(Long doctorId, Long keepRelId) {
        UpdateWrapper<DoctorDepartmentRelEntity> wrapper = new UpdateWrapper<DoctorDepartmentRelEntity>()
                .eq("doctor_id", doctorId)
                .eq("is_deleted", 0)
                .set("is_primary", 0);
        if (keepRelId != null) {
            wrapper.ne("id", keepRelId);
        }
        doctorDepartmentRelMapper.update(null, wrapper);
    }

    private void syncAccountPrimaryIfNeeded(Long doctorId, String centerCode, String departmentCode,
                                            String departmentName, boolean primaryRequested) {
        if (!primaryRequested) {
            return;
        }
        StaffAccountEntity account = staffAccountMapper.selectById(doctorId);
        if (account == null) {
            return;
        }
        account.setCenterCode(centerCode);
        account.setDepartmentCode(departmentCode);
        account.setDepartmentName(departmentName);
        staffAccountMapper.updateById(account);
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), message);
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private void throwDuplicateDepartment() {
        throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该医生已绑定此中心科室");
    }
}
