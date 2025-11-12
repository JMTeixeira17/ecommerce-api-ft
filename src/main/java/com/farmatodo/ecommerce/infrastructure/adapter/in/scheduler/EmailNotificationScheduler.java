package com.farmatodo.ecommerce.infrastructure.adapter.in.scheduler;

import com.farmatodo.ecommerce.domain.port.in.EmailNotificationProcessorUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationScheduler {

    private final EmailNotificationProcessorUseCase emailProcessorUseCase;

    @Scheduled(fixedRate = 60000)
    public void triggerEmailProcessing() {
        log.info("Scheduled job 'triggerEmailProcessing' started...");
        try {
            emailProcessorUseCase.processPendingEmails();
        } catch (Exception e) {
            log.error("Error during scheduled email processing: {}", e.getMessage(), e);
        }
        log.info("Scheduled job 'triggerEmailProcessing' finished.");
    }
}