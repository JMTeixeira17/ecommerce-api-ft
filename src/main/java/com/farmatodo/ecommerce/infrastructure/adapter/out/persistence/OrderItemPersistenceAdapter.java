package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.OrderItem;
import com.farmatodo.ecommerce.domain.port.out.OrderItemRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.OrderItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderItemPersistenceAdapter implements OrderItemRepositoryPort {

    private final OrderItemJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public List<OrderItem> saveAll(List<OrderItem> items) {
        var entities = items.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        var savedEntities = jpaRepository.saveAll(entities);
        return savedEntities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}