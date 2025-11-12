package com.farmatodo.ecommerce.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemRequest {

    @NotNull(message = "El ID del producto no puede ser nulo")
    private Long productId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
}