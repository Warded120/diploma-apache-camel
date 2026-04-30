package com.ivan.inbound.exception;

public class OrderMappingException extends RuntimeException {
    public OrderMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}

