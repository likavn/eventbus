package com.github.likavn.eventbus.core.exception;


/**
 * Base RuntimeException for errors
 *
 * @author likavn
 * @date 2024/01/01
 */
public class EventBusException extends RuntimeException {

    public EventBusException(String message) {
        super(message);
    }

    public EventBusException(Throwable cause) {
        super(cause);
    }

    public EventBusException(String message, Throwable cause) {
        super(message, cause);
    }

}
