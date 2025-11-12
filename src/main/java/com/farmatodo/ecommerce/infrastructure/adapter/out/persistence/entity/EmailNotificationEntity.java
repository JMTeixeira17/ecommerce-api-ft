package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity;

import com.farmatodo.ecommerce.domain.model.EmailNotification.EmailStatus;
import com.farmatodo.ecommerce.domain.model.EmailNotification.EmailType;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GeneratedColumn;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_notifications")
@Data
public class EmailNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GeneratedColumn("DEFAULT")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false,
            columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID uuid;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "email_to", nullable = false)
    private String emailTo;

    @Column(name = "email_subject", nullable = false, length = 500)
    private String emailSubject;

    @Column(name = "email_body", nullable = false, columnDefinition = "TEXT")
    private String emailBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EmailStatus status;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}