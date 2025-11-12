package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.domain.model.Payment;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("SELECT p FROM PaymentEntity p " +
            "WHERE p.orderId = :orderId " +
            "AND p.status IN ('PENDING', 'DECLINED') " +
            "ORDER BY p.attemptNumber DESC " +
            "LIMIT 1")
    Optional<PaymentEntity> findPendingPaymentByOrderId(Long orderId);
}