package com.farmatodo.ecommerce.application.event.listener;

import com.farmatodo.ecommerce.application.event.PaymentSuccessEvent;
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
public class PaymentSuccessEventListener {

    private final EmailNotificationRepositoryPort notificationRepository;

    @Async
    @EventListener
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        log.info("Received PaymentSuccessEvent for orderId: {}", event.getOrderId());
        try {
            String subject = "¡Tu pago ha sido aprobado! Pedido #" + event.getOrderNumber();
            String body = String.format(
                    "<p>Hola,</p>" +
                            "<p>¡Buenas noticias! Tu pago para el pedido <strong>#%s</strong> ha sido aprobado.</p>" +
                            "<p>Pronto estaremos preparando tu envío.</p>" +
                            "<p>Gracias,<br>El equipo de Farmatodo</p>",
                    event.getOrderNumber()
            );

            EmailNotification notification = EmailNotification.builder()
                    .customerId(event.getCustomerId())
                    .orderId(event.getOrderId())
                    .paymentId(event.getPaymentId())
                    .emailTo(event.getCustomerEmail())
                    .emailSubject(subject)
                    .emailBody(body)
                    .emailType(EmailNotification.EmailType.PAYMENT_SUCCESS)
                    .status(EmailNotification.EmailStatus.PENDING)
                    .retryCount(0)
                    .maxRetries(3)
                    .build();

            notificationRepository.save(notification);
            log.info("Email notification (PAYMENT_SUCCESS) for orderId: {} enqueued successfully.", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to enqueue email notification for orderId: {}", event.getOrderId(), e);
        }
    }
}