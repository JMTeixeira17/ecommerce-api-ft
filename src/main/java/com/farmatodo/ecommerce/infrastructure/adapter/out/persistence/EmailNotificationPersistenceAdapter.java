package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.EmailNotification;
import com.farmatodo.ecommerce.domain.port.out.EmailNotificationRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.EmailNotificationEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.EmailNotificationPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.EmailNotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmailNotificationPersistenceAdapter implements EmailNotificationRepositoryPort {

    private final EmailNotificationJpaRepository jpaRepository;
    private final EmailNotificationPersistenceMapper mapper;

    @Override
    public EmailNotification save(EmailNotification notification) {
        EmailNotificationEntity entity = mapper.toEntity(notification);
        EmailNotificationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }


    @Override
    public List<EmailNotification> findPendingEmailsToRetry() {
        List<EmailNotificationEntity> entities = jpaRepository.findPendingEmailsToRetry(
                EmailNotification.EmailStatus.PENDING
        );
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}