package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.CartItem;
import com.farmatodo.ecommerce.domain.port.out.CartItemRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.ShoppingCartPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.CartItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartItemPersistenceAdapter implements CartItemRepositoryPort {

    private final CartItemJpaRepository jpaRepository;
    private final ShoppingCartPersistenceMapper mapper;

    @Override
    public Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId) {
        return jpaRepository.findByCartIdAndProductId(cartId, productId)
                .map(mapper::toDomain);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        var entity = mapper.toEntity(cartItem);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}