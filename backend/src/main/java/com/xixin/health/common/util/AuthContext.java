package com.xixin.health.common.util;

import com.xixin.health.common.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证上下文工具类 - 基于ThreadLocal存储当前登录用户信息
 */
public final class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<LoginUser>();

    private AuthContext() {
    }

    /** 设置当前登录用户 */
    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /** 获取当前登录用户 */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前登录账号ID */
    public static Long getAccountId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getAccountId();
    }

    /** 获取当前登录用户ID */
    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /** 获取当前登录用户角色 */
    public static RoleType getRole() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getRole();
    }

    /** 清除上下文 */
    public static void clear() {
        HOLDER.remove();
    }

    /** 登录用户信息 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginUser {
        private Long accountId;
        private Long userId;
        private String username;
        private String displayName;
        private RoleType role;
    }
}
