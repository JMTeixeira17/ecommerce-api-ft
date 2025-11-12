package com.farmatodo.ecommerce.application.dto;

import com.farmatodo.ecommerce.domain.model.Payment;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
public class PaymentResponse {
    private UUID paymentUuid;
    private UUID orderUuid;
    private Payment.PaymentStatus status;
    private int attemptNumber;
    private int maxAttempts;
    private BigDecimal amount;
    private String failureReason;
    private String transactionId;
}