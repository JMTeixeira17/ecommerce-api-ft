package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TransactionLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogsJpaRepository extends JpaRepository<TransactionLogsEntity, Long> {
}