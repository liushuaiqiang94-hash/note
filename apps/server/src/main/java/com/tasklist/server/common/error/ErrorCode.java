package com.tasklist.server.common.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(40000, "Bad request"),
    UNAUTHORIZED(40100, "Unauthorized"),
    FORBIDDEN(40300, "Forbidden"),
    NOT_FOUND(40400, "Resource not found"),
    VALIDATION_ERROR(42200, "Validation failed"),
    WECHAT_LOGIN_FAILED(50010, "WeChat login failed"),
    SYSTEM_ERROR(50000, "System error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
