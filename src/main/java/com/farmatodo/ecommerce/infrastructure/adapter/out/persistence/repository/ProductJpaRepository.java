package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByNameContainingIgnoreCaseAndStockGreaterThanAndIsActiveTrue(
            String name,
            int stock
    );

    Optional<ProductEntity> findByIdAndIsActiveTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id AND p.isActive = true")
    Optional<ProductEntity> findActiveByIdAndLock(@Param("id") Long id);
}