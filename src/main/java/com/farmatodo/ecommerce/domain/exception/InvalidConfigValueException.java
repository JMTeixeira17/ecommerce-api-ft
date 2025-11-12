package com.farmatodo.ecommerce.domain.exception;

public class InvalidConfigValueException extends RuntimeException {
    public InvalidConfigValueException(String message) {
        super(message);
    }
}