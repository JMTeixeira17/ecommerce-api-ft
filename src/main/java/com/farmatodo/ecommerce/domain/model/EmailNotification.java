package com.farmatodo.ecommerce.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class EmailNotification {

    private Long id;
    private UUID uuid;
    private Long customerId;
    private Long orderId;
    private Long paymentId;
    private String emailTo;
    private String emailSubject;
    private String emailBody;
    private EmailType emailType;
    private EmailStatus status;
    private OffsetDateTime sentAt;
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum EmailStatus {
        PENDING,
        SENT,
        FAILED,
        CANCELLED
    }
    public enum EmailType {
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        ORDER_CONFIRMATION,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        WELCOME
    }
}