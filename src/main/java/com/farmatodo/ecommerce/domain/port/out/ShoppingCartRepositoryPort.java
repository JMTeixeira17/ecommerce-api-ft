package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import java.util.Optional;


public interface ShoppingCartRepositoryPort {

    Optional<ShoppingCart> findActiveByCustomerId(Long customerId);

    Optional<ShoppingCart> findActiveBySessionId(String sessionId);

    ShoppingCart save(ShoppingCart shoppingCart);

    Optional<ShoppingCart> findByIdWithItems(Long id);
}