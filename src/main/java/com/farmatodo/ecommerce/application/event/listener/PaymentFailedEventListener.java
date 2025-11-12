package com.farmatodo.ecommerce.application.event.listener;

import com.farmatodo.ecommerce.application.event.PaymentFailedEvent;
import com.farmatodo.ecommerce.domain.model.EmailNotification;
import com.farmatodo.ecommerce.domain.port.out.EmailNotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedEventListener {

    private final EmailNotificationRepositoryPort notificationRepository;

    @Async
    @EventListener
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for orderId: {}", event.getOrderId());
        try {
            String subject = "Problema con tu pago para el pedido #" + event.getOrderId();
            String body = String.format(
                    "<p>Hola,</p>" +
                            "<p>Tuvimos un problema al procesar tu pago para el pedido <strong>#%d</strong>.</p>" +
                            "<p><strong>Motivo del fallo:</strong> %s</p>" +
                            "<p>Por favor, revisa tus datos de pago e inténtalo de nuevo.</p>" +
                            "<p>Gracias,<br>El equipo de Farmatodo</p>",
                    event.getOrderId(),
                    event.getFailureReason()
            );

            EmailNotification notification = EmailNotification.builder()
                    .customerId(event.getCustomerId())
                    .orderId(event.getOrderId())
                    .paymentId(event.getPaymentId())
                    .emailTo(event.getCustomerEmail())
                    .emailSubject(subject)
                    .emailBody(body)
                    .emailType(EmailNotification.EmailType.PAYMENT_FAILED)
                    .status(EmailNotification.EmailStatus.PENDING)
                    .retryCount(0)
                    .maxRetries(3)
                    .build();

            notificationRepository.save(notification);
            log.info("Email notification (PAYMENT_FAILED) for orderId: {} enqueued successfully.", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to enqueue email notification for orderId: {}", event.getOrderId(), e);
        }
    }
}