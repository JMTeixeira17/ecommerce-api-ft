package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.TransactionLogs;
import com.farmatodo.ecommerce.domain.port.out.TransactionLogsRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TransactionLogsEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.TransactionLogsPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.TransactionLogsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TransactionLogsPersistenceAdapter implements TransactionLogsRepositoryPort {

    private final TransactionLogsJpaRepository jpaRepository;
    private final TransactionLogsPersistenceMapper mapper;

    @Override
    public void save(TransactionLogs log) {
        TransactionLogsEntity entity = mapper.toEntity(log);
        jpaRepository.save(entity);
    }
}