package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.Payment;
import java.util.Optional;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);
    Optional<Payment> findPendingPaymentByOrderId(Long orderId);
}