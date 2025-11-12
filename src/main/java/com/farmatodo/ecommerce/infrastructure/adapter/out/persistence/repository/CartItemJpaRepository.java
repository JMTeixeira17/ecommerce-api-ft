package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, Long> {
    Optional<CartItemEntity> findByCartIdAndProductId(Long cartId, Long productId);
}