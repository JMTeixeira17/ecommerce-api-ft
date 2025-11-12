package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByEmail(String email);
    Optional<CustomerEntity> findByPhone(String phone);
    boolean existsByEmail(String email);
}