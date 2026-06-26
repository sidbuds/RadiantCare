package com.xixin.health.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private Integer code;
    private String message;
    private T data;

    /** 成功响应 */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(0, "success", data);
    }

    /** 成功响应(无数据) */
    public static <T> ApiResult<T> success() {
        return success(null);
    }

    /** 失败响应 */
    public static <T> ApiResult<T> fail(Integer code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
