package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.SystemConfigRequest;
import com.farmatodo.ecommerce.application.usecase.UpdateSystemConfigUseCaseImpl;
import com.farmatodo.ecommerce.domain.model.SystemConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Tag(name = "Configuración del Sistema", description = "Gestión de parámetros de configuración de negocio.")
public class SystemConfigController {

    private final UpdateSystemConfigUseCaseImpl updateSystemConfigUseCase;

    @Operation(
            summary = "Actualizar configuración del sistema",
            description = "Modifica un parámetro de configuración del sistema por su clave. Requiere validaciones específicas (ej. 0.0 a 1.0 para probabilidades de rechazo).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Clave de configuración y nuevo valor.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SystemConfigRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Configuración actualizada con éxito",
                            content = @Content(schema = @Schema(implementation = SystemConfig.class))),
                    @ApiResponse(responseCode = "400", description = "Parámetro o valor inválido (ej. probabilidad > 1.0)"),
                    @ApiResponse(responseCode = "404", description = "Clave de configuración no encontrada"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @PutMapping
    public ResponseEntity<SystemConfig> updateConfig(@Valid @RequestBody SystemConfigRequest request) {
        SystemConfig updatedConfig = updateSystemConfigUseCase.updateConfig(request);
        return new ResponseEntity<>(updatedConfig, HttpStatus.OK);
    }
}