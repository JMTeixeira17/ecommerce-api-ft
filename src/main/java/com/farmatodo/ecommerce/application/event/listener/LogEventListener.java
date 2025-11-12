package com.farmatodo.ecommerce.application.event.listener;

import com.farmatodo.ecommerce.application.event.LogApiEvent;
import com.farmatodo.ecommerce.domain.model.TransactionLogs;
import com.farmatodo.ecommerce.domain.port.out.TransactionLogsRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final TransactionLogsRepositoryPort transactionLogsRepositoryPort;

    @Async("asyncExecutor")
    @EventListener
    public void handleLogApiEvent(LogApiEvent event) {
        try {
            log.info("Processing async log event for endpoint: {}", event.getEndpoint());

            TransactionLogs logEntry = TransactionLogs.builder()
                    .httpMethod(event.getHttpMethod())
                    .endpoint(event.getEndpoint())
                    .statusCode(event.getStatusCode())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .executionTimeMs(event.getExecutionTimeMs())
                    .customerId(event.getCustomerId())
                    .responseBody(event.getResponseBody())
                    .success(event.isSuccess())
                    .errorMessage(event.getErrorMessage())
                    .stackTrace(event.getStackTrace())
                    .build();

            transactionLogsRepositoryPort.save(logEntry);
            log.info("Successfully saved async log for: {}", event.getEndpoint());

        } catch (Exception e) {
            log.error("Failed to save async log event: {}", e.getMessage(), e);
        }
    }
}