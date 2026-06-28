package com.xixin.health.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.auth.dto.LoginRequest;
import com.xixin.health.auth.dto.RegisterRequest;
import com.xixin.health.auth.entity.StaffAccountEntity;
import com.xixin.health.auth.entity.StaffRoleRelEntity;
import com.xixin.health.auth.mapper.StaffAccountMapper;
import com.xixin.health.auth.mapper.StaffRoleRelMapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.enums.RoleType;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务 - 处理登录/注册逻辑
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final StaffAccountMapper staffAccountMapper;
    private final StaffRoleRelMapper staffRoleRelMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper,
                       StaffAccountMapper staffAccountMapper,
                       StaffRoleRelMapper staffRoleRelMapper) {
        this.userMapper = userMapper;
        this.staffAccountMapper = staffAccountMapper;
        this.staffRoleRelMapper = staffRoleRelMapper;
    }

    /** 用户注册 */
    @Transactional
    public AuthContext.LoginUser register(RegisterRequest request) {
        String username = request.getUsername();
        assertUsernameAvailable(username);
        assertMobileAvailable(request.getMobile());

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setUserNo(username);
        user.setName(request.getName());
        user.setMobile(request.getMobile());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setLastLoginAt(now);
        user.setIsDeleted(0);
        userMapper.insert(user);

        return new AuthContext.LoginUser(
                user.getId(),
                user.getId(),
                user.getUserNo(),
                user.getName(),
                RoleType.USER
        );
    }

    /** 用户登录(支持用户名/手机号) */
    public AuthContext.LoginUser login(LoginRequest request) {
        StaffAccountEntity staffAccount = staffAccountMapper.selectOne(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getUsername, request.getUsername())
                .eq(StaffAccountEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (staffAccount != null) {
            return loginStaff(staffAccount, request.getPassword());
        }

        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .and(wrapper -> wrapper
                        .eq(UserEntity::getUserNo, request.getUsername())
                        .or()
                        .eq(UserEntity::getMobile, request.getUsername()))
                .eq(UserEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (user != null) {
            return loginUser(user, request.getPassword());
        }
        throw new BizException(ErrorCode.LOGIN_FAILED);
    }

    private void assertUsernameAvailable(String username) {
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserNo, username)
                .eq(UserEntity::getIsDeleted, 0));
        Long staffCount = staffAccountMapper.selectCount(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getUsername, username)
                .eq(StaffAccountEntity::getIsDeleted, 0));
        if ((userCount != null && userCount > 0) || (staffCount != null && staffCount > 0)) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "用户名已存在");
        }
    }

    private void assertMobileAvailable(String mobile) {
        Long mobileCount = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getMobile, mobile)
                .eq(UserEntity::getIsDeleted, 0));
        if (mobileCount != null && mobileCount > 0) {
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "手机号已存在");
        }
    }

    private AuthContext.LoginUser loginStaff(StaffAccountEntity staffAccount, String rawPassword) {
        if (staffAccount.getStatus() == null || staffAccount.getStatus() != 1) {
            throw new BizException(ErrorCode.LOGIN_FAILED.getCode(), "账号已被封禁");
        }
        if (!passwordEncoder.matches(rawPassword, staffAccount.getPasswordHash())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        List<StaffRoleRelEntity> roles = staffRoleRelMapper.selectList(new LambdaQueryWrapper<StaffRoleRelEntity>()
                .eq(StaffRoleRelEntity::getStaffAccountId, staffAccount.getId())
                .eq(StaffRoleRelEntity::getIsDeleted, 0)
                .orderByAsc(StaffRoleRelEntity::getId));
        if (roles.isEmpty()) {
            throw new BizException(ErrorCode.STAFF_ROLE_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        staffAccountMapper.update(null, new LambdaUpdateWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getId, staffAccount.getId())
                .set(StaffAccountEntity::getLastLoginAt, now));
        return new AuthContext.LoginUser(
                staffAccount.getId(),
                staffAccount.getBindUserId(),
                staffAccount.getUsername(),
                staffAccount.getDisplayName(),
                RoleType.valueOf(roles.get(0).getRoleCode())
        );
    }

    private AuthContext.LoginUser loginUser(UserEntity user, String rawPassword) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ErrorCode.LOGIN_FAILED.getCode(), "账号已被封禁");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        LocalDateTime now = LocalDateTime.now();
        userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getId, user.getId())
                .set(UserEntity::getLastLoginAt, now));

        // 检查该用户是否有关联的 staff_account（通过 admin 赋予身份创建的）
        StaffAccountEntity staffAccount = staffAccountMapper.selectOne(new LambdaQueryWrapper<StaffAccountEntity>()
                .eq(StaffAccountEntity::getBindUserId, user.getId())
                .eq(StaffAccountEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (staffAccount != null && staffAccount.getStatus() != null && staffAccount.getStatus() == 1) {
            List<StaffRoleRelEntity> roles = staffRoleRelMapper.selectList(new LambdaQueryWrapper<StaffRoleRelEntity>()
                    .eq(StaffRoleRelEntity::getStaffAccountId, staffAccount.getId())
                    .eq(StaffRoleRelEntity::getIsDeleted, 0)
                    .orderByAsc(StaffRoleRelEntity::getId));
            if (!roles.isEmpty()) {
                return new AuthContext.LoginUser(
                        staffAccount.getId(),
                        staffAccount.getBindUserId(),
                        staffAccount.getUsername(),
                        staffAccount.getDisplayName(),
                        RoleType.valueOf(roles.get(0).getRoleCode())
                );
            }
        }

        return new AuthContext.LoginUser(
                user.getId(),
                user.getId(),
                user.getUserNo(),
                user.getName(),
                RoleType.USER
        );
    }
}
