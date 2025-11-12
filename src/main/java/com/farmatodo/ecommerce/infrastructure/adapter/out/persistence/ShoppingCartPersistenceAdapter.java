package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import com.farmatodo.ecommerce.domain.port.out.ShoppingCartRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.ShoppingCartPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.ShoppingCartJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShoppingCartPersistenceAdapter implements ShoppingCartRepositoryPort {

    private final ShoppingCartJpaRepository jpaRepository;
    private final ShoppingCartPersistenceMapper mapper;

    @Override
    public Optional<ShoppingCart> findActiveByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerIdAndStatus(customerId, ShoppingCart.CartStatus.ACTIVE)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ShoppingCart> findActiveBySessionId(String sessionId) {
        return jpaRepository.findBySessionIdAndStatus(sessionId, ShoppingCart.CartStatus.ACTIVE)
                .map(mapper::toDomain);
    }

    @Override
    public ShoppingCart save(ShoppingCart shoppingCart) {
        var entity = mapper.toEntity(shoppingCart);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ShoppingCart> findByIdWithItems(Long id) {
        return jpaRepository.findByIdWithItems(id)
                .map(mapper::toDomain);
    }
}