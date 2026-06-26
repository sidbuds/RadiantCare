package com.xixin.health.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.common.util.AuthContext;
import com.xixin.health.user.dto.ChangePasswordRequest;
import com.xixin.health.user.dto.UpdateProfileRequest;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserProfileService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserProfileService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Map<String, Object> getProfile() {
        UserEntity user = getCurrentUser();
        Map<String, Object> result = new HashMap<>();
        result.put("userNo", user.getUserNo());
        result.put("name", user.getName());
        result.put("gender", user.getGender());
        result.put("birthDate", user.getBirthDate());
        result.put("idType", user.getIdType());
        result.put("idNo", maskIdNo(user.getIdNo()));
        result.put("mobile", maskMobile(user.getMobile()));
        result.put("email", user.getEmail());
        result.put("address", user.getAddress());
        result.put("emergencyContact", user.getEmergencyContact());
        result.put("emergencyMobile", user.getEmergencyMobile());
        return result;
    }

    public void updateProfile(UpdateProfileRequest request) {
        UserEntity user = getCurrentUser();
        user.setName(request.getName());
        user.setGender(request.getGender());
        if (request.getBirthDate() != null && !request.getBirthDate().isEmpty()) {
            user.setBirthDate(java.time.LocalDate.parse(request.getBirthDate()));
        }
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setEmergencyContact(request.getEmergencyContact());
        user.setEmergencyMobile(request.getEmergencyMobile());
        userMapper.updateById(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        UserEntity user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    private UserEntity getCurrentUser() {
        Long userId = AuthContext.getUserId();
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .eq(UserEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    private String maskIdNo(String idNo) {
        if (idNo == null || idNo.length() < 8) return idNo;
        return idNo.substring(0, 3) + "***" + idNo.substring(idNo.length() - 4);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) return mobile;
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
