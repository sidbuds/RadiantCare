package com.xixin.health.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PARAM_INVALID(1001, "参数校验失败"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    OPERATION_CONFLICT(1003, "操作冲突"),
    UNAUTHORIZED(2001, "用户未登录"),
    FORBIDDEN(2002, "无操作权限"),
    OPERATOR_CAPACITY_INVALID(2003, "运营排班容量配置不合法"),
    APPOINTMENT_FULL(3001, "该时段已约满"),
    ORDER_STATUS_INVALID(4001, "订单状态不合法"),
    REFUND_APPLY_NOT_FOUND(4002, "退款申请不存在"),
    REFUND_STATUS_INVALID(4003, "退款申请状态不合法"),
    REFUND_DUPLICATE_APPLY(4004, "订单存在未完成退款申请"),
    REFUND_ORDER_NOT_ELIGIBLE(4005, "当前订单不满足退款条件"),
    EXAM_TASK_NOT_FOUND(5001, "体检任务不存在"),
    DOCTOR_ASSIGN_INVALID(5002, "当前检查项未分配到该医生"),
    REPORT_REVIEW_REQUIRED(5501, "报告未审核通过"),
    CONSULTATION_NOT_FOUND(7001, "咨询单不存在"),
    CONSULTATION_STATUS_INVALID(7002, "咨询单状态不合法"),
    PACKAGE_ITEM_REQUIRED(8001, "套餐至少包含一个检查项"),
    STAFF_ROLE_NOT_FOUND(8002, "后台账号角色不存在"),
    LOGIN_FAILED(9001, "登录失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
