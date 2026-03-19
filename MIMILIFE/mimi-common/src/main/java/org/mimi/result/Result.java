package org.mimi.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果，格式为：code, data, message
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 数据
     */
    private T data;

    /**
     * 消息
     */
    private String message;

    private Result() {
    }

    /**
     * 成功
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 成功
     */
    public static <T> Result<T> ok(T data) {
        return ok("操作成功", data);
    }

    /**
     * 成功
     */
    public static <T> Result<T> ok(String message, T data) {
        String msg = message == null ? "操作成功" : message;
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS);
        result.setMessage(msg);
        result.setData(data);
        return result;
    }

    /**
     * 失败
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(String message) {
        String msg = message == null ? "操作失败" : message;
        return error(ResultCode.ERROR, msg);
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(int code, String message) {
        String msg = message == null ? "系统异常" : message;
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(msg);
        result.setData(null);
        return result;
    }
}
