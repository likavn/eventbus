package com.github.likavn.eventbus.core.exception;


/**
 * Base RuntimeException for errors that occur when executing AMQP operations.
 *
 * @author likavn
 * @date 2023/12/22
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
