/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.core.base;

import com.github.likavn.eventbus.core.annotation.Order;
import com.github.likavn.eventbus.core.api.interceptor.*;
import com.github.likavn.eventbus.core.exception.DeliverAfterInterceptorSuccessException;
import com.github.likavn.eventbus.core.exception.DeliverBeforeInterceptorException;
import com.github.likavn.eventbus.core.metadata.data.Request;
import com.github.likavn.eventbus.core.utils.Func;
import lombok.Setter;

import java.util.List;

/**
 * 全局拦截器容器
 *
 * @author likavn
 * @date 2024/01/01
 **/
@Setter
@SuppressWarnings("all")
public class InterceptorContainer {
    private final List<SendBeforeInterceptor> sendBeforeInterceptors;
    private final List<SendAfterInterceptor> sendAfterInterceptors;
    private final List<DeliverBeforeInterceptor> deliverBeforeInterceptors;
    private final List<DeliverAfterInterceptor> deliverAfterInterceptors;
    private final List<DeliverThrowableLastInterceptor> deliverThrowableLastInterceptors;

    public InterceptorContainer(List<SendBeforeInterceptor> sendBeforeInterceptors,
                                List<SendAfterInterceptor> sendAfterInterceptors,
                                List<DeliverBeforeInterceptor> deliverBeforeInterceptors,
                                List<DeliverAfterInterceptor> deliverAfterInterceptors,
                                List<DeliverThrowableLastInterceptor> deliverThrowableLastInterceptors) {
        this.sendBeforeInterceptors = sendBeforeInterceptors;
        sort(this.sendBeforeInterceptors);
        this.sendAfterInterceptors = sendAfterInterceptors;
        sort(this.sendAfterInterceptors);
        this.deliverBeforeInterceptors = deliverBeforeInterceptors;
        sort(this.deliverBeforeInterceptors);
        this.deliverAfterInterceptors = deliverAfterInterceptors;
        sort(this.deliverAfterInterceptors);
        this.deliverThrowableLastInterceptors = deliverThrowableLastInterceptors;
        sort(this.deliverThrowableLastInterceptors);
    }

    /**
     * 发送前拦截
     *
     * @param request 请求
     */
    public void sendBeforeExecute(Request<?> request) {
        // 只有第一次发送才执行拦截器
        if (sendBeforeInterceptors != null) {
            convertRequest(request);
            sendBeforeInterceptors.forEach(interceptor -> interceptor.execute((Request<String>) request));
        }
    }

    /**
     * 发送后拦截
     *
     * @param request 请求
     */
    public void sendAfterExecute(Request<?> request) {
        // 只有第一次发送才执行拦截器
        if (sendAfterInterceptors != null) {
            convertRequest(request);
            sendAfterInterceptors.forEach(interceptor -> interceptor.execute((Request<String>) request));
        }
    }

    /**
     * 接收前拦截
     *
     * @param request 请求
     */
    public void deliverBeforeExecute(Request<?> request) {
        if (deliverBeforeInterceptors != null) {
            try {
                convertRequest(request);
                deliverBeforeInterceptors.forEach(interceptor -> interceptor.execute((Request<String>) request));
            } catch (Exception e) {
                throw new DeliverBeforeInterceptorException(e);
            }
        }
    }

    /**
     * 接收后拦截
     *
     * @param request 请求
     */
    public void deliverAfterSuccessExecute(Request<?> request) {
        if (deliverAfterInterceptors != null) {
            try {
                convertRequest(request);
                deliverAfterInterceptors.forEach(interceptor -> interceptor.execute((Request<String>) request, null));
            } catch (Exception e) {
                throw new DeliverAfterInterceptorSuccessException(e);
            }
        }
    }

    /**
     * 接收后拦截
     *
     * @param request   请求
     * @param throwable 异常
     */
    public void deliverAfterExceptionExecute(Request<?> request, Throwable throwable) {
        if (deliverAfterInterceptors != null) {
            convertRequest(request);
            deliverAfterInterceptors.forEach(interceptor -> interceptor.execute((Request<String>) request, throwable));
        }
    }

    /**
     * 接收异常拦截
     * <p>
     * 最后一次还是异常时会调用
     *
     * @param request   请求
     * @param throwable 异常
     */
    public void deliverThrowableLastExecute(Request<?> request, Throwable throwable) {
        if (deliverThrowableLastInterceptors != null) {
            convertRequest(request);
            deliverThrowableLastInterceptors.forEach(interceptor -> interceptor.execute((Request<String>) request, throwable));
        }
    }

    /**
     * 将请求体转换为json
     *
     * @param request 请求
     */
    private void convertRequest(Request request) {
        if (!(request.getBody() instanceof String)) {
            request.setBody(Func.toJson(request.getBody()));
        }
    }

    /**
     * 排序
     *
     * @param interceptors 拦截器
     */
    private void sort(List<?> interceptors) {
        if (Func.isEmpty(interceptors)) {
            return;
        }
        interceptors.sort((o1, o2) -> getOrder(o1) - getOrder(o2));
    }

    /**
     * 获取排序
     *
     * @param o 对象
     * @return 排序
     */
    private int getOrder(Object o) {
        Order order = o.getClass().getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }
}
