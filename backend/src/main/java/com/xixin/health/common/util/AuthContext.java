package com.xixin.health.common.util;

import com.xixin.health.common.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<LoginUser>();

    private AuthContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getAccountId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getAccountId();
    }

    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static RoleType getRole() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }

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
