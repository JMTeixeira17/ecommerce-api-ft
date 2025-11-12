package com.farmatodo.ecommerce.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private UUID uuid;
    private Long orderId;
    private Long tokenizedCardId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private int attemptNumber;
    private int maxAttempts;
    private String transactionId;
    private String paymentGatewayResponse;
    private String failureReason;
    private OffsetDateTime processedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Order order;
    private TokenizedCard tokenizedCard;

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        APPROVED,
        DECLINED,
        FAILED,
        REFUNDED
    }
}