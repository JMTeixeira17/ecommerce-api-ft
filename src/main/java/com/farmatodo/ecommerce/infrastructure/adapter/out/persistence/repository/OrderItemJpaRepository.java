package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {
}