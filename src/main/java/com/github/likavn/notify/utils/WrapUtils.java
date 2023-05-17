package com.github.likavn.notify.utils;

import com.alibaba.fastjson.JSON;
import com.github.likavn.notify.domain.Request;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 包装转换工具
 *
 * @author likavn
 * @since 2023/01/01
 */
@Slf4j
public class WrapUtils {

    private WrapUtils() {
    }

    /**
     * toJson
     *
     * @param value object
     * @return string
     */
    public static String toJson(Object value) {
        return JSON.toJSONString(value);
    }

    /**
     * 二进制数据转换为实体数据类型
     *
     * @param requestBytes bytes
     * @return bean
     */
    @SuppressWarnings("all")
    public static Request convertByBytes(String requestStr) {
        return convertByBytes(requestStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 二进制数据转换为实体数据类型
     *
     * @param requestBytes bytes
     * @return bean
     */
    @SuppressWarnings("all")
    public static Request convertByBytes(byte[] requestBytes) {
        Request request = JSON.parseObject(requestBytes, Request.class);
        Object body = request.getBody();
        if (body instanceof Map) {
            request.setBody(JSON.parseObject(JSON.toJSONString(body), request.getBodyClass()));
            return request;
        }
        return request;
    }

}
