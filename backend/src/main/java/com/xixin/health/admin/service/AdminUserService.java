package com.xixin.health.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.user.entity.UserEntity;
import com.xixin.health.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {

    private final UserMapper userMapper;

    public AdminUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Map<String, Object> listUsers(String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getIsDeleted, 0)
                .orderByDesc(UserEntity::getId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(UserEntity::getName, keyword)
                    .or().like(UserEntity::getMobile, keyword)
                    .or().like(UserEntity::getUserNo, keyword));
        }
        List<UserEntity> users = userMapper.selectList(wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserEntity u : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("userNo", u.getUserNo());
            item.put("name", u.getName());
            item.put("gender", u.getGender());
            item.put("mobile", maskMobile(u.getMobile()));
            item.put("email", u.getEmail());
            item.put("status", u.getStatus());
            item.put("lastLoginAt", u.getLastLoginAt());
            item.put("createdAt", u.getCreatedAt());
            list.add(item);
        }
        int total = list.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list.subList(from, to));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    public void updateUserStatus(Long userId, Integer status) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) return mobile;
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
