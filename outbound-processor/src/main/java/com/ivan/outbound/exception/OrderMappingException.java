package com.ivan.outbound.exception;

public class OrderMappingException extends RuntimeException {
    public OrderMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}

