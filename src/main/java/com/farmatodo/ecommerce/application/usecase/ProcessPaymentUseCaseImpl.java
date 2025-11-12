package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.PaymentResponse;
import com.farmatodo.ecommerce.application.event.PaymentFailedEvent;
import com.farmatodo.ecommerce.application.event.PaymentSuccessEvent;
import com.farmatodo.ecommerce.application.mapper.PaymentApiMapper;
import com.farmatodo.ecommerce.domain.exception.OrderNotFoundException;
import com.farmatodo.ecommerce.domain.exception.PaymentException;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.Order;
import com.farmatodo.ecommerce.domain.model.Payment;
import com.farmatodo.ecommerce.domain.port.in.ProcessPaymentUseCase;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.OrderRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.PaymentRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCaseImpl implements ProcessPaymentUseCase {

    private final OrderRepositoryPort orderRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final CustomerRepositoryPort customerRepository;
    private final SystemConfigRepositoryPort systemConfigRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentApiMapper paymentApiMapper;

    private static final String REJECTION_KEY = "payment.rejection.probability";
    private static final double DEFAULT_REJECTION_PROB = 0.15;

    @Override
    @Transactional(noRollbackFor = PaymentException.class)
    public PaymentResponse processOrderPayment(String orderUuid) {
        Customer customer = getAuthenticatedCustomer();
        Order order = orderRepository.findByUuidAndCustomerId(UUID.fromString(orderUuid), customer.getId())
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada o no pertenece al usuario."));

        Payment payment = paymentRepository.findPendingPaymentByOrderId(order.getId())
                .orElseThrow(() -> new OrderNotFoundException("No se encontró un pago pendiente para esta orden."));

        payment.setOrder(order);

        if (order.getStatus() == Order.OrderStatus.PAID) {
            throw new PaymentException("Esta orden ya ha sido pagada.");
        }
        if (order.getStatus() == Order.OrderStatus.PAYMENT_FAILED) {
            throw new PaymentException("Esta orden ya ha fallado todos los intentos de pago.");
        }

        log.info("Procesando pago (Intento {}/{}) para la orden: {}",
                payment.getAttemptNumber(), payment.getMaxAttempts(), order.getOrderNumber());

        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        order.setStatus(Order.OrderStatus.PAYMENT_PROCESSING);
        paymentRepository.save(payment);
        orderRepository.save(order);

        double rejectionProbability = systemConfigRepository.getValueAsDouble(REJECTION_KEY, DEFAULT_REJECTION_PROB);

        try {
            simulatePaymentGatewayCall(rejectionProbability);

            log.info("Pago APROBADO para la orden: {}", order.getOrderNumber());
            payment.setStatus(Payment.PaymentStatus.APPROVED);
            payment.setTransactionId("txn_" + UUID.randomUUID().toString().substring(0, 12));
            payment.setProcessedAt(OffsetDateTime.now());
            payment.setFailureReason(null);
            order.setStatus(Order.OrderStatus.PAID);
            order.setCompletedAt(OffsetDateTime.now());
            paymentRepository.save(payment);
            orderRepository.save(order);
            eventPublisher.publishEvent(new PaymentSuccessEvent(
                    customer.getId(), customer.getEmail(), order.getId(), payment.getId(), order.getOrderNumber()
            ));

        } catch (PaymentException e) {
            log.warn("Pago RECHAZADO para la orden: {}. Motivo: {}", order.getOrderNumber(), e.getMessage());

            payment.setFailureReason(e.getMessage());
            payment.setProcessedAt(OffsetDateTime.now());
            payment.setAttemptNumber(payment.getAttemptNumber() + 1);

            if (payment.getAttemptNumber() >= payment.getMaxAttempts()) {
                log.error("Pago FAILED (máximos intentos) para la orden: {}", order.getOrderNumber());
                payment.setStatus(Payment.PaymentStatus.FAILED);
                order.setStatus(Order.OrderStatus.PAYMENT_FAILED);
                eventPublisher.publishEvent(new PaymentFailedEvent(
                        customer.getId(), customer.getEmail(), order.getId(), payment.getId(), "Máximos intentos de pago alcanzados."
                ));

            } else {
                log.warn("Pago DECLINED (reintentable) para la orden: {}", order.getOrderNumber());
                payment.setStatus(Payment.PaymentStatus.DECLINED);
                order.setStatus(Order.OrderStatus.PENDING);
            }

            paymentRepository.save(payment);
            orderRepository.save(order);

            throw new PaymentException(String.format(
                    "Pago %s. (Intento %d/%d). Motivo: %s",
                    payment.getStatus().name(),
                    payment.getAttemptNumber(),
                    payment.getMaxAttempts(),
                    e.getMessage()
            ));
        }
        return paymentApiMapper.toResponse(payment);
    }

    private void simulatePaymentGatewayCall(double rejectionProbability) throws PaymentException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        if (Math.random() < rejectionProbability) {
            throw new PaymentException("Fondos insuficientes (Rechazo simulado)");
        }
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Se requiere autenticación de cliente para esta operación.");
        }
        String email = ((User) authentication.getPrincipal()).getUsername();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Cliente autenticado no encontrado en la base de datos."));
    }
}