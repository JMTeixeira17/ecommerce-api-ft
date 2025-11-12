package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ShoppingCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShoppingCartJpaRepository extends JpaRepository<ShoppingCartEntity, Long> {
    @Query("SELECT sc FROM ShoppingCartEntity sc LEFT JOIN FETCH sc.items WHERE sc.customerId = :customerId AND sc.status = :status")
    Optional<ShoppingCartEntity> findByCustomerIdAndStatus(
            @Param("customerId") Long customerId,
            @Param("status") ShoppingCart.CartStatus status
    );
    @Query("SELECT sc FROM ShoppingCartEntity sc LEFT JOIN FETCH sc.items WHERE sc.sessionId = :sessionId AND sc.status = :status")
    Optional<ShoppingCartEntity> findBySessionIdAndStatus(
            @Param("sessionId") String sessionId,
            @Param("status") ShoppingCart.CartStatus status
    );

    @Query("SELECT sc FROM ShoppingCartEntity sc LEFT JOIN FETCH sc.items ci LEFT JOIN FETCH ci.product WHERE sc.id = :id")
    Optional<ShoppingCartEntity> findByIdWithItems(@Param("id") Long id);
}