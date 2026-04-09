package com.vod.server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权或Token失效"),
    FORBIDDEN(403, "无权限访问"),
    SYSTEM_ERROR(500, "系统内部异常"),
    BIZ_ERROR(600, "业务逻辑异常");

    private final Integer code;
    private final String msg;
}