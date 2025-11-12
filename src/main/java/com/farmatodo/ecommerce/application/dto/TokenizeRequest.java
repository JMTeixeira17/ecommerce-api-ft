package com.farmatodo.ecommerce.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class TokenizeRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Numero de tarjeta inválido")
    private String cardNumber;

    @NotBlank
    @Size(min = 3, max = 4)
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV inválido")
    private String cvv;

    @NotBlank
    private String cardHolderName;

    @NotBlank(message = "El mes de expiración es requerido")
    @Pattern(regexp = "^(0?[1-9]|1[0-2])$", message = "Mes de expiración invalido (deberia ser1-12 o 01-12)")
    private String expirationMonth;

    @NotBlank
    @Pattern(regexp = "^(20[2-9][0-9])$", message = "Año de expiración invalido (YYYY)")
    private String expirationYear;
}