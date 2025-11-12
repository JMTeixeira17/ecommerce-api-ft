package com.farmatodo.ecommerce.domain.port.out;


public interface EmailSenderPort {

    void sendEmail(String to, String subject, String body) throws Exception;
}