package com.xixin.health.common.exception;

import com.xixin.health.common.api.ApiResult;
import com.xixin.health.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBiz(BizException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return ApiResult.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResult<Void> handleValidate(Exception ex) {
        String fieldErrors;
        if (ex instanceof MethodArgumentNotValidException) {
            fieldErrors = ((MethodArgumentNotValidException) ex).getBindingResult()
                    .getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            fieldErrors = ((BindException) ex).getBindingResult()
                    .getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        log.warn("参数校验失败: {}", fieldErrors);
        return ApiResult.fail(ErrorCode.PARAM_INVALID.getCode(),
                ErrorCode.PARAM_INVALID.getMessage() + " - " + fieldErrors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("缺少请求参数: {}", ex.getParameterName());
        return ApiResult.fail(ErrorCode.PARAM_INVALID.getCode(),
                "缺少必要参数: " + ex.getParameterName());
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ApiResult.fail(ErrorCode.PARAM_INVALID.getCode(), ErrorCode.PARAM_INVALID.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Void> handleAccessDenied(AccessDeniedException ex) {
        log.warn("访问被拒绝: {}", ex.getMessage());
        return ApiResult.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<Void> handleAuthentication(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return ApiResult.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("不支持的请求方法: {}", ex.getMethod());
        return ApiResult.fail(1004, "不支持的请求方法: " + ex.getMethod());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleOther(Exception ex) {
        log.error("系统异常", ex);
        return ApiResult.fail(9000, "系统内部错误，请稍后重试");
    }
}
