package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, Long> {
    Optional<SystemConfigEntity> findByConfigKeyAndIsActiveTrue(String configKey);
}