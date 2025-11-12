package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.domain.model.EmailNotification;
import com.farmatodo.ecommerce.domain.port.in.EmailNotificationProcessorUseCase;
import com.farmatodo.ecommerce.domain.port.out.EmailNotificationRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationProcessorUseCaseImpl implements EmailNotificationProcessorUseCase {

    private final EmailNotificationRepositoryPort notificationRepository;
    private final EmailSenderPort emailSender;

    @Override
    @Transactional
    public void processPendingEmails() {
        log.debug("Procesando emails pendientes.");

        List<EmailNotification> pendingEmails = notificationRepository.findPendingEmailsToRetry();

        if (pendingEmails.isEmpty()) {
            log.debug("No hay emails pendientes por enviar.");
            return;
        }

        log.info("Hay {} emails pendientes por enviar..", pendingEmails.size());

        for (EmailNotification email : pendingEmails) {
            boolean sent = false;
            String errorMsg = null;

            try {
                emailSender.sendEmail(
                        email.getEmailTo(),
                        email.getEmailSubject(),
                        email.getEmailBody()
                );
                sent = true;
                log.info("Email enviado satisfactoriamente(ID: {}) to {}", email.getId(), email.getEmailTo());

            } catch (Exception e) {
                log.warn("Error al enviar el email (ID: {}) to {}. Intento {}/{}",
                        email.getId(), email.getEmailTo(), email.getRetryCount() + 1, email.getMaxRetries(), e);
                errorMsg = e.getMessage();
            }

            updateNotificationStatus(email, sent, errorMsg);
        }
    }

    private void updateNotificationStatus(EmailNotification email, boolean sent, String errorMsg) {
        if (sent) {
            email.setStatus(EmailNotification.EmailStatus.SENT);
            email.setSentAt(OffsetDateTime.now());
            email.setErrorMessage(null);
        } else {
            email.setRetryCount(email.getRetryCount() + 1);
            email.setErrorMessage(errorMsg);

            if (email.getRetryCount() >= email.getMaxRetries()) {
                email.setStatus(EmailNotification.EmailStatus.FAILED);
                log.error("Email (ID: {}) al {} falló despues de {} intentos.",
                        email.getId(), email.getEmailTo(), email.getMaxRetries());
            } else {
                email.setStatus(EmailNotification.EmailStatus.PENDING);
            }
        }

        notificationRepository.save(email);
    }
}