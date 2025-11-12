package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Sistema", description = "Servicios de salud y monitoreo del sistema")
public class PingController {

    @Operation(summary = "Verificación de salud",
            description = "Endpoint simple para verificar si el servicio está en línea.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Servicio activo.")
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}