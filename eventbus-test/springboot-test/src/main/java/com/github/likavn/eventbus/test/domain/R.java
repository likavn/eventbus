package com.github.likavn.eventbus.test.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author likavn
 * @date 2024/1/20
 **/
@Data
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private boolean success;
    private T data;
    private String msg;

    public static <T> R<T> ok(T data) {
        final R<T> tr = new R<>();
        tr.setCode(200);
        tr.setSuccess(true);
        tr.setData(data);
        return tr;
    }

    public static <T> R<T> fail(String msg) {
        final R<T> tr = new R<>();
        tr.setCode(400);
        tr.setSuccess(false);
        tr.setMsg(msg);
        return tr;
    }
}
