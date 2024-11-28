package com.smart.neural.controller.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Result<E> implements Serializable {
    @Serial private static final long serialVersionUID = -511998552713152813L;
    private boolean success;    // 请求是否成功
    private String message;     // 错误信息描述
    private E data;             // 返回数据

    public Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String error) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(error);
        return result;
    }
}
