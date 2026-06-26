package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.auth.entity.DoctorDepartmentRelEntity;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.DoctorDepartmentRelMapper;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminDoctorService(StaffAccountMapper staffAccountMapper,
                              StaffRoleRelMapper staffRoleRelMapper,
                              DoctorDepartmentRelMapper doctorDepartmentRelMapper) {
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.doctorDepartmentRelMapper = doctorDepartmentRelMapper;
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
        List<Map<String, Object>> doctors = new ArrayList<>();
        for (StaffAccountEntity account : all) {
            if (!isDoctorAccount(account.getId())) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", account.getId());
            item.put("username", account.getUsername());
            item.put("displayName", account.getDisplayName());
            item.put("departmentCode", account.getDepartmentCode());
            item.put("departmentName", account.getDepartmentName());
            item.put("specialty", account.getSpecialty());
            item.put("centerCode", account.getCenterCode());
            item.put("status", account.getStatus());
            item.put("createdAt", account.getCreatedAt());
            // 获取绑定的科室列表
            List<DoctorDepartmentRelEntity> rels = doctorDepartmentRelMapper.selectList(
                    new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                            .eq(DoctorDepartmentRelEntity::getDoctorId, account.getId())
                            .eq(DoctorDepartmentRelEntity::getIsDeleted, 0));
            item.put("departments", rels);
            List<StaffRoleRelEntity> roleRels = staffRoleRelMapper.selectList(
                    new LambdaQueryWrapper<StaffRoleRelEntity>()
                            .eq(StaffRoleRelEntity::getStaffAccountId, account.getId())
                            .eq(StaffRoleRelEntity::getIsDeleted, 0));
            List<String> roles = new ArrayList<String>();
            for (StaffRoleRelEntity roleRel : roleRels) {
                roles.add(roleRel.getRoleCode());
            }
            item.put("roles", roles);
            doctors.add(item);
        }
        int total = doctors.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<>();
        result.put("list", doctors.subList(from, to));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Transactional
    public void updateDoctor(Long id, Map<String, Object> data) {
        StaffAccountEntity account = staffAccountMapper.selectById(id);
        if (account == null) throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "医生不存在");
        if (data.containsKey("displayName")) account.setDisplayName((String) data.get("displayName"));
        if (data.containsKey("departmentCode")) account.setDepartmentCode((String) data.get("departmentCode"));
        if (data.containsKey("departmentName")) account.setDepartmentName((String) data.get("departmentName"));
        if (data.containsKey("specialty")) account.setSpecialty((String) data.get("specialty"));
        if (data.containsKey("centerCode")) account.setCenterCode((String) data.get("centerCode"));
        if (data.containsKey("status")) account.setStatus((Integer) data.get("status"));
        staffAccountMapper.updateById(account);
    }

    @Transactional
    public void bindDepartment(Long doctorId, String departmentCode, String departmentName, String centerCode, Boolean isPrimary) {
        // 检查是否已绑定
        DoctorDepartmentRelEntity existing = doctorDepartmentRelMapper.selectOne(
                new LambdaQueryWrapper<DoctorDepartmentRelEntity>()
                        .eq(DoctorDepartmentRelEntity::getDoctorId, doctorId)
                        .eq(DoctorDepartmentRelEntity::getDepartmentCode, departmentCode)
                        .eq(DoctorDepartmentRelEntity::getIsDeleted, 0)
                        .last("limit 1"));
        if (existing != null) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该医生已绑定此科室");
        }
        DoctorDepartmentRelEntity rel = new DoctorDepartmentRelEntity();
        rel.setDoctorId(doctorId);
        rel.setDepartmentCode(departmentCode);
        rel.setDepartmentName(departmentName);
        rel.setCenterCode(centerCode);
        rel.setIsPrimary(Boolean.TRUE.equals(isPrimary) ? 1 : 0);
        rel.setIsDeleted(0);
        doctorDepartmentRelMapper.insert(rel);
    }

    @Transactional
    public void unbindDepartment(Long relId) {
        doctorDepartmentRelMapper.update(null, new LambdaUpdateWrapper<DoctorDepartmentRelEntity>()
                .eq(DoctorDepartmentRelEntity::getId, relId)
                .set(DoctorDepartmentRelEntity::getIsDeleted, 1));
    }

    private boolean isDoctorAccount(Long accountId) {
        Long count = staffRoleRelMapper.selectCount(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, accountId)
                .eq(StaffRoleRelEntity::getRoleCode, "DOCTOR")
                .eq(StaffRoleRelEntity::getIsDeleted, 0));
        return count != null && count > 0;
    }
}
