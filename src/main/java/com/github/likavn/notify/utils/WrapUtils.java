package com.github.likavn.notify.utils;

import com.alibaba.fastjson.JSON;
import com.github.likavn.notify.domain.MetaRequest;
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
    @SuppressWarnings("all")
    public static String toJson(Object value) {
        try {
            return JSON.toJSONString(value);
        } catch (Exception ex) {
            log.error("WrapUtils.toJson", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * json string to object
     *
     * @param requestBytes object
     * @return string
     */
    @SuppressWarnings("all")
    public static <T> T readJson(byte[] requestBytes, Class<T> valueType) {
        try {
            return JSON.parseObject(requestBytes, valueType);
        } catch (Exception ex) {
            log.error("WrapUtils.readJson", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 二进制数据转换为实体数据类型
     *
     * @param requestBytes bytes
     * @return bean
     */
    @SuppressWarnings("all")
    public static MetaRequest convertByBytes(String requestBytes) {
        return convertByBytes(requestBytes.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 二进制数据转换为实体数据类型
     *
     * @param requestBytes bytes
     * @return bean
     */
    @SuppressWarnings("all")
    public static MetaRequest convertByBytes(byte[] requestBytes) {
        MetaRequest request = readJson(requestBytes, MetaRequest.class);
        Object body = request.getBody();
        if (body instanceof Map) {
            try {
                request.setBody(JSON.parseObject(JSON.toJSONString(body), request.getBodyClass()));
                return request;
            } catch (Exception e) {
                log.error("WrapUtils.convertByBytes", e);
            }
        }
        return request;
    }

}
