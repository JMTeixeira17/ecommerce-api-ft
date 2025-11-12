package com.farmatodo.ecommerce.domain.exception;

public class SystemConfigNotFoundException extends RuntimeException {
    public SystemConfigNotFoundException(String message) {
        super(message);
    }
}