package com.farmatodo.ecommerce.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RegisterCardResponse {
    private UUID cardId;
    private String lastFourDigits;
    private String cardBrand;
    private boolean isDefault;
}