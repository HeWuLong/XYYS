package com.vod.server.utils;
import com.vod.server.exception.ErrorCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String msg;
    private T data;
    public static <T> Result<T> success() {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMsg(), null);
    }
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMsg(), data);
    }
    public static <T> Result<T> error(String msg) {
        return new Result<>(ErrorCodeEnum.BIZ_ERROR.getCode(), msg, null);
    }
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
    public static <T> Result<T> error(ErrorCodeEnum errorCodeEnum) {
        return new Result<>(errorCodeEnum.getCode(), errorCodeEnum.getMsg(), null);
    }
}