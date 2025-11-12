package com.farmatodo.ecommerce.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {
    private Long id;
    private UUID uuid;
    private Long customerId;
    private String sessionId;
    private CartStatus status;
    private BigDecimal totalAmount;
    private int totalItems;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime expiresAt;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public enum CartStatus {
        ACTIVE,
        EXPIRED,
        CONVERTED
    }
}