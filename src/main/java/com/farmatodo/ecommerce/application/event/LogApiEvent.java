package com.farmatodo.ecommerce.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogApiEvent {
    private String httpMethod;
    private String endpoint;
    private Integer statusCode;
    private String ipAddress;
    private String userAgent;
    private Integer executionTimeMs;
    private Long customerId;
    private String responseBody;
    private boolean success;
    private String errorMessage;
    private String stackTrace;
}