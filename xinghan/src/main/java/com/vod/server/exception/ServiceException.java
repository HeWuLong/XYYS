package com.vod.server.exception;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final ErrorCodeEnum errorCode;
    public ServiceException(ErrorCodeEnum errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
    public ServiceException(ErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(ErrorCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}