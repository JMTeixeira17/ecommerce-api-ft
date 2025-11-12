package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.Payment;
import com.farmatodo.ecommerce.domain.port.out.PaymentRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.PaymentPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    @Override
    public Payment save(Payment payment) {
        var entity = mapper.toEntity(payment);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findPendingPaymentByOrderId(Long orderId) {
        return jpaRepository.findPendingPaymentByOrderId(orderId)
                .map(mapper::toDomain);
    }
}