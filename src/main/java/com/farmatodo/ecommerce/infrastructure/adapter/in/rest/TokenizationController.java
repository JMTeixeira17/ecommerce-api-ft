package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;
import com.farmatodo.ecommerce.domain.port.in.TokenizeCardUseCase;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tokenize")
@RequiredArgsConstructor
@Tag(name = "System", description = "Componente interno de Tokenización (API Key only)")
public class TokenizationController {

    private final TokenizeCardUseCase tokenizeCardUseCase;

    @Operation(summary = "Componente de Tokenización de tarjeta",
            description = "Recibe datos sensibles (card, cvv, exp) y devuelve un token. Requiere header X-API-KEY.")
    @Parameter(in = ParameterIn.HEADER, name = "X-API-KEY", description = "API Key requerida para autenticar el servicio.", required = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tokenización exitosa.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de tarjeta inválidos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "API Key faltante o incorrecta.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Tokenización rechazada por probabilidad (simulado).")
    @PostMapping
    public ResponseEntity<ApiResponse<TokenizeResponse>> tokenizeCard(
            @Valid @RequestBody TokenizeRequest request
    ) {
        TokenizeResponse response = tokenizeCardUseCase.tokenize(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}