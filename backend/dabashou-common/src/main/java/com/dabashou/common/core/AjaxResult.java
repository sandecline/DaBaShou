package com.dabashou.common.core;

import com.dabashou.common.enums.ErrorCode;

/**
 * 统一响应包装类
 *
 * @param <T> 数据类型
 */
public class AjaxResult<T> {

    private int code;
    private String msg;
    private T data;

    public AjaxResult() {
    }

    public AjaxResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> AjaxResult<T> ok() {
        return new AjaxResult<>(200, "success", null);
    }

    public static <T> AjaxResult<T> ok(T data) {
        return new AjaxResult<>(200, "success", data);
    }

    public static <T> AjaxResult<T> ok(String msg, T data) {
        return new AjaxResult<>(200, msg, data);
    }

    public static <T> AjaxResult<T> fail(ErrorCode errorCode) {
        return new AjaxResult<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    public static <T> AjaxResult<T> fail(int code, String msg) {
        return new AjaxResult<>(code, msg, null);
    }

    public static <T> AjaxResult<T> fail(ErrorCode errorCode, String msg) {
        return new AjaxResult<>(errorCode.getCode(), msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
