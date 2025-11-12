package com.farmatodo.ecommerce.domain.exception;


public class InvalidCardDataException extends RuntimeException {
    public InvalidCardDataException(String message) {
        super(message);
    }
}