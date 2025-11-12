package com.farmatodo.ecommerce.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {
    private UUID itemUuid;
    private UUID productUuid;
    private String productName;
    private String productSku;
    private String productImageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}