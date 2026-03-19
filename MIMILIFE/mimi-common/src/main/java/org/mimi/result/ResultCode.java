package org.mimi.result;

/**
 * 结果状态码
 */
public class ResultCode {
    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int ERROR = 500;

    /**
     * 未认证
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 未授权
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    public static final int NOT_FOUND = 404;

    /**
     * 请求方法不支持
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * 请求参数错误
     */
    public static final int PARAM_ERROR = 400;
}
