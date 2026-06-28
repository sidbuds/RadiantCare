package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.service.AuditLogService;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminRoleService {

    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    public AdminRoleService(StaffAccountMapper staffAccountMapper,
                            StaffRoleRelMapper staffRoleRelMapper,
                            UserMapper userMapper,
                            AuditLogService auditLogService) {
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    public List<Map<String, Object>> listAccounts() {
        List<StaffAccountEntity> accounts = staffAccountMapper.selectList(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getIsDeleted, 0)
                .orderByDesc(StaffAccountEntity::getId));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (StaffAccountEntity account : accounts) {
            List<StaffRoleRelEntity> roleRels = staffRoleRelMapper.selectList(new LambdaQueryWrapper<StaffRoleRelEntity>()
                    .eq(StaffRoleRelEntity::getStaffAccountId, account.getId())
                    .eq(StaffRoleRelEntity::getIsDeleted, 0)
                    .orderByAsc(StaffRoleRelEntity::getId));
            List<String> roles = new ArrayList<String>();
            for (StaffRoleRelEntity roleRel : roleRels) {
                roles.add(roleRel.getRoleCode());
            }
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", account.getId());
            item.put("username", account.getUsername());
            item.put("displayName", account.getDisplayName());
            item.put("status", account.getStatus());
            item.put("departmentCode", account.getDepartmentCode());
            item.put("departmentName", account.getDepartmentName());
            item.put("centerCode", account.getCenterCode());
            item.put("roles", roles);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void bindRole(Long staffAccountId, String roleCode) {
        if (staffAccountId == null || roleCode == null || roleCode.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "参数不完整");
        }
        StaffAccountEntity account = staffAccountMapper.selectById(staffAccountId);
        if (account == null || Integer.valueOf(1).equals(account.getIsDeleted())) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "账号不存在");
        }
        StaffRoleRelEntity existing = getActiveRole(staffAccountId);
        if (existing != null) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该账号已设置身份，请先解绑后再设置");
        }
        StaffRoleRelEntity rel = new StaffRoleRelEntity();
        rel.setStaffAccountId(staffAccountId);
        rel.setRoleCode(roleCode.trim().toUpperCase());
        rel.setIsDeleted(0);
        staffRoleRelMapper.insert(rel);
        auditLogService.record("ROLE", "BIND", "STAFF_ACCOUNT", staffAccountId);
    }

    @Transactional
    public void unbindRole(Long staffAccountId, String roleCode) {
        if (staffAccountId == null || roleCode == null || roleCode.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "参数不完整");
        }
        int updated = staffRoleRelMapper.update(null, new LambdaUpdateWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, staffAccountId)
                .eq(StaffRoleRelEntity::getRoleCode, roleCode)
                .eq(StaffRoleRelEntity::getIsDeleted, 0)
                .set(StaffRoleRelEntity::getIsDeleted, 1));
        if (updated == 0) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "角色绑定不存在");
        }
        auditLogService.record("ROLE", "UNBIND", "STAFF_ACCOUNT", staffAccountId);
    }

    @Transactional
    public Map<String, Object> grantUserRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null || roleCode.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "参数不完整");
        }
        String normalizedRole = roleCode.trim().toUpperCase();
        if (!"DOCTOR".equals(normalizedRole) && !"OPERATOR".equals(normalizedRole)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "只能赋予医生或运营身份");
        }

        UserEntity user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }

        StaffAccountEntity account = staffAccountMapper.selectOne(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getBindUserId, userId)
                .eq(StaffAccountEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (account == null) {
            account = new StaffAccountEntity();
            account.setUsername(user.getUserNo());
            account.setPasswordHash(user.getPasswordHash());
            account.setDisplayName(user.getName());
            account.setBindUserId(userId);
            account.setStatus(1);
            account.setIsDeleted(0);
            staffAccountMapper.insert(account);
        } else if (account.getStatus() == null || account.getStatus() != 1) {
            account.setStatus(1);
            staffAccountMapper.updateById(account);
        }

        StaffRoleRelEntity existing = getActiveRole(account.getId());
        if (existing != null && !normalizedRole.equals(existing.getRoleCode())) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "该账号已设置其他身份，请先解绑后再设置");
        }
        if (existing == null) {
            StaffRoleRelEntity rel = new StaffRoleRelEntity();
            rel.setStaffAccountId(account.getId());
            rel.setRoleCode(normalizedRole);
            rel.setIsDeleted(0);
            staffRoleRelMapper.insert(rel);
        }
        auditLogService.record("ROLE", "BIND", "STAFF_ACCOUNT", account.getId());

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("staffAccountId", account.getId());
        result.put("userId", userId);
        result.put("roleCode", normalizedRole);
        return result;
    }

    private StaffRoleRelEntity getActiveRole(Long staffAccountId) {
        return staffRoleRelMapper.selectOne(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, staffAccountId)
                .eq(StaffRoleRelEntity::getIsDeleted, 0)
                .last("limit 1"));
    }
}
