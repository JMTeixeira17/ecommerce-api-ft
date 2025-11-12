package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.Order;
import com.farmatodo.ecommerce.domain.port.out.OrderRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        var entity = mapper.toEntity(order);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findByUuidAndCustomerId(UUID uuid, Long customerId) {
        return jpaRepository.findByUuidAndCustomerIdWithItems(uuid, customerId)
                .map(mapper::toDomain);
    }
}