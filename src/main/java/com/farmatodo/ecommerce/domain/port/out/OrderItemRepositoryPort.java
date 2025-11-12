package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.OrderItem;
import java.util.List;

public interface OrderItemRepositoryPort {
    List<OrderItem> saveAll(List<OrderItem> items);
}