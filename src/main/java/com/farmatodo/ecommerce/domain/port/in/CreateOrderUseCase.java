package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.CreateOrderRequest;
import com.farmatodo.ecommerce.application.dto.OrderResponse;


public interface CreateOrderUseCase {
    OrderResponse createOrderFromCart(CreateOrderRequest request);
}