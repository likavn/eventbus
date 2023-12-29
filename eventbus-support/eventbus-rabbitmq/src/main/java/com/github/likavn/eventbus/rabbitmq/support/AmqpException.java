package com.github.likavn.eventbus.rabbitmq.support;


/**
 * Base RuntimeException for errors that occur when executing AMQP operations.
 *
 * @author likavn
 * @date 2024/01/01
 */
public class AmqpException extends RuntimeException {

    public AmqpException(String message) {
        super(message);
    }

    public AmqpException(Throwable cause) {
        super(cause);
    }

    public AmqpException(String message, Throwable cause) {
        super(message, cause);
    }

}
