package com.farmatodo.ecommerce.domain.exception;


public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}