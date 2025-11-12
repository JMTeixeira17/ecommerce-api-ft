package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.CartItem;
import java.util.Optional;


public interface CartItemRepositoryPort {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    CartItem save(CartItem cartItem);
}