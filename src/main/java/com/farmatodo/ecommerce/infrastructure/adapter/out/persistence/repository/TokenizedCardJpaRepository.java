package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TokenizedCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenizedCardJpaRepository extends JpaRepository<TokenizedCardEntity, Long> {

    boolean existsByCustomerIdAndLastFourDigitsAndCardBrand(
            Long customerId,
            String lastFourDigits,
            String cardBrand
    );
    List<TokenizedCardEntity> findByCustomerIdAndIsActiveTrue(Long customerId);
    Optional<TokenizedCardEntity> findByUuidAndCustomerId(UUID uuid, Long customerId);
}