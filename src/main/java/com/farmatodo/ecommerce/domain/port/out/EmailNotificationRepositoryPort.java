package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.EmailNotification;

import java.util.List;


public interface EmailNotificationRepositoryPort {

    EmailNotification save(EmailNotification notification);
    List<EmailNotification> findPendingEmailsToRetry();
}