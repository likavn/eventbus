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
package com.github.likavn.eventbus.core.exception;


/**
 * eventbus delivery interceptor success exception
 *
 * @author likavn
 * @date 2025/03/04
 * @since 2.5.1
 */
public class DeliverAfterInterceptorSuccessException extends RuntimeException {

    public DeliverAfterInterceptorSuccessException(String message) {
        super(message);
    }

    public DeliverAfterInterceptorSuccessException(Throwable cause) {
        super(cause);
    }

    public DeliverAfterInterceptorSuccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
