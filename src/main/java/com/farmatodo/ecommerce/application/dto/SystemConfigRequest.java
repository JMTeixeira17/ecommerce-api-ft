package com.farmatodo.ecommerce.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemConfigRequest {

    @NotBlank(message = "La clave de configuración no puede estar vacía.")
    private String configKey;

    @NotBlank(message = "El valor de configuración no puede estar vacío.")
    private String configValue;
}