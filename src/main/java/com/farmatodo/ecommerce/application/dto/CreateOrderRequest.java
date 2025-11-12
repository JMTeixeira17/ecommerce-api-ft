package com.farmatodo.ecommerce.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "El UUID de la tarjeta no puede estar vacío")
    @NotNull(message = "El UUID de la tarjeta no puede ser nulo")
    private String tokenizedCardUuid;

    @NotBlank(message = "addressLine1 no puede estar vacío")
    private String shippingAddressLine1;

    private String shippingAddressLine2;

    @NotBlank(message = "city no puede estar vacío")
    private String shippingCity;

    @NotBlank(message = "state no puede estar vacío")
    private String shippingState;

    @NotBlank(message = "postalCode no puede estar vacío")
    private String shippingPostalCode;

    @Size(min = 2, max = 2, message = "country debe tener 2 caracteres")
    private String shippingCountry = "MX";
}