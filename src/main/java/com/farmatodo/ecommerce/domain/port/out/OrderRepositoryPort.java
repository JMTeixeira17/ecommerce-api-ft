package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findByUuidAndCustomerId(UUID uuid, Long customerId);
}