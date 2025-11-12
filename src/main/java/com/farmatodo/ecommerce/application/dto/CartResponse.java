package com.farmatodo.ecommerce.application.dto;

import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {
    private UUID cartUuid;
    private String sessionId;
    private ShoppingCart.CartStatus status;
    private BigDecimal totalAmount;
    private int totalItems;
    private List<CartItemResponse> items;
}