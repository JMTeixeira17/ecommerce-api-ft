package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.uuid = :uuid AND o.customerId = :customerId")
    Optional<OrderEntity> findByUuidAndCustomerIdWithItems(UUID uuid, Long customerId);
}