package com.farmatodo.ecommerce.domain.exception;


public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String message) {
        super(message);
    }
}