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
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.OrderRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.PaymentRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentUseCaseImplTest {

    @Mock private OrderRepositoryPort orderRepository;
    @Mock private PaymentRepositoryPort paymentRepository;
    @Mock private CustomerRepositoryPort customerRepository;
    @Mock private SystemConfigRepositoryPort systemConfigRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private PaymentApiMapper paymentApiMapper;

    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private ProcessPaymentUseCaseImpl processPaymentUseCase;

    private Customer customer;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).email("test@user.com").build();
        order = Order.builder().id(1L).uuid(UUID.randomUUID()).status(Order.OrderStatus.PENDING).build();
        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .status(Payment.PaymentStatus.PENDING)
                .attemptNumber(1)
                .maxAttempts(3)
                .amount(new BigDecimal("100"))
                .order(order)
                .build();
    }

    private void mockSecurityContext() {
        User userPrincipal = new User(customer.getEmail(), "", Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        SecurityContextHolder.setContext(securityContext);
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
    }

    @Test
    void whenProcessPayment_isApproved_shouldReturnApprovedAndPublishEvent() {
        mockSecurityContext();
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findPendingPaymentByOrderId(1L)).thenReturn(Optional.of(payment));
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(0.0);
        when(paymentApiMapper.toResponse(any(Payment.class))).thenReturn(PaymentResponse.builder().status(Payment.PaymentStatus.APPROVED).build());
        PaymentResponse response = processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        assertEquals(Payment.PaymentStatus.APPROVED, response.getStatus());
        verify(orderRepository, times(2)).save(argThat(o -> o.getStatus() == Order.OrderStatus.PAID || o.getStatus() == Order.OrderStatus.PAYMENT_PROCESSING));
        verify(paymentRepository, times(2)).save(argThat(p -> p.getStatus() == Payment.PaymentStatus.APPROVED || p.getStatus() == Payment.PaymentStatus.PROCESSING));
        verify(eventPublisher, times(1)).publishEvent(any(PaymentSuccessEvent.class));
    }

    @Test
    void whenProcessPayment_isDeclined_shouldReturnDeclinedAndIncrementAttempt() {
        mockSecurityContext();
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findPendingPaymentByOrderId(1L)).thenReturn(Optional.of(payment));
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(1.0);

        Exception exception = assertThrows(PaymentException.class, () -> {
            processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        });
        assertTrue(exception.getMessage().contains("DECLINED"));
        assertTrue(exception.getMessage().contains("Intento 2/3"));
        verify(paymentRepository, times(2)).save(argThat(p ->
                (p.getStatus() == Payment.PaymentStatus.PROCESSING) ||
                        (p.getStatus() == Payment.PaymentStatus.DECLINED && p.getAttemptNumber() == 2)
        ));
        verify(eventPublisher, never()).publishEvent(any(PaymentFailedEvent.class));
    }

    @Test
    void whenProcessPayment_isFailed_shouldReturnFailedAndPublishEvent() {
        mockSecurityContext();
        payment.setAttemptNumber(3);
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findPendingPaymentByOrderId(1L)).thenReturn(Optional.of(payment));
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(1.0);
        Exception exception = assertThrows(PaymentException.class, () -> {
            processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        });
        assertTrue(exception.getMessage().contains("FAILED"));
        assertTrue(exception.getMessage().contains("Intento 4/3"));
        verify(paymentRepository, times(2)).save(argThat(p ->
                (p.getStatus() == Payment.PaymentStatus.PROCESSING) ||
                        (p.getStatus() == Payment.PaymentStatus.FAILED && p.getAttemptNumber() == 4)
        ));
        verify(eventPublisher, times(1)).publishEvent(any(PaymentFailedEvent.class));
    }

    @Test
    void whenProcessPayment_orderIsAlreadyPaid_shouldThrowPaymentException() {
        mockSecurityContext();
        order.setStatus(Order.OrderStatus.PAID);
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findPendingPaymentByOrderId(1L)).thenReturn(Optional.of(payment));
        Exception exception = assertThrows(PaymentException.class, () -> {
            processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        });
        assertEquals("Esta orden ya ha sido pagada.", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void whenProcessPayment_orderHasFailedPermanently_shouldThrowPaymentException() {
        mockSecurityContext();
        order.setStatus(Order.OrderStatus.PAYMENT_FAILED);
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findPendingPaymentByOrderId(1L)).thenReturn(Optional.of(payment));
        Exception exception = assertThrows(PaymentException.class, () -> {
            processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        });
        assertEquals("Esta orden ya ha fallado todos los intentos de pago.", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void whenProcessPayment_orderNotFoundForUser_shouldThrowOrderNotFoundException() {
        mockSecurityContext();
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> {
            processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        });
        verify(paymentRepository, never()).findPendingPaymentByOrderId(anyLong());
    }

    @Test
    void whenProcessPayment_pendingPaymentNotFound_shouldThrowOrderNotFoundException() {
        mockSecurityContext();
        when(orderRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findPendingPaymentByOrderId(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> {
            processPaymentUseCase.processOrderPayment(order.getUuid().toString());
        });
        verify(paymentRepository, times(1)).findPendingPaymentByOrderId(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }
}