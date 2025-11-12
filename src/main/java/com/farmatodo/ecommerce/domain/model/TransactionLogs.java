package com.farmatodo.ecommerce.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogs {
    private Long id;
    private UUID uuid;
    private String httpMethod;
    private String endpoint;
    private String responseBody;
    private Integer statusCode;
    private Long customerId;
    private String ipAddress;
    private String userAgent;
    private Integer executionTimeMs;
    private boolean success;
    private String errorMessage;
    private String stackTrace;
    private OffsetDateTime createdAt;
}