
package com.ivan.inbound.exception;

public class InvalidOrderIdException extends RuntimeException {
    public InvalidOrderIdException(String message) {
        super(message);
    }
}

