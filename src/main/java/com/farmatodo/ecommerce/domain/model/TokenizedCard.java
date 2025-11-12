package com.farmatodo.ecommerce.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@Builder
public class TokenizedCard {
    private Long id;
    private UUID uuid;
    private Long customerId;
    private String token;
    private String lastFourDigits;
    private String cardBrand;
    private String cardholderName;
    private String expirationMonth;
    private String expirationYear;
    private Boolean isActive;
    private boolean isDefault;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime lastUsedAt;
}