package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.domain.model.EmailNotification.EmailStatus;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.EmailNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmailNotificationJpaRepository extends JpaRepository<EmailNotificationEntity, Long> {

    @Query("SELECT e FROM EmailNotificationEntity e " +
            "WHERE e.status = :status " +
            "AND e.retryCount < e.maxRetries " +
            "ORDER BY e.createdAt ASC")
    List<EmailNotificationEntity> findPendingEmailsToRetry(@Param("status") EmailStatus status);
}