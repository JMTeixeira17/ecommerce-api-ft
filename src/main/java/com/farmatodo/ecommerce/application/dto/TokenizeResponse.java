package com.farmatodo.ecommerce.application.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TokenizeResponse {
    private String token;
    private String cardBrand;
    private String lastFourDigits;
}