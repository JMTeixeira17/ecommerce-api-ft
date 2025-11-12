package com.farmatodo.ecommerce.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private UUID uuid;
    private Long orderId;
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private OffsetDateTime createdAt;
    private Product product;
}