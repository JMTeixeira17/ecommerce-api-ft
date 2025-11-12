package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.LoginRequest;
import com.farmatodo.ecommerce.application.dto.RegisterCustomerRequest;
import com.farmatodo.ecommerce.domain.port.in.LoginCustomerUseCase;
import com.farmatodo.ecommerce.domain.port.in.RegisterCustomerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticación de usuarios")
public class AuthController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final LoginCustomerUseCase loginCustomerUseCase;

    @Operation(summary = "Registrar un nuevo cliente",
            description = "Crea un nuevo usuario, hashea la contraseña y devuelve un JWT.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registro exitoso. Devuelve JWT.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud inválida (Validación DTO).",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email o teléfono ya registrados.",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterCustomerRequest request
    ) {
        AuthResponse authResponse = registerCustomerUseCase.register(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, 201));
    }

    @Operation(summary = "Iniciar sesión del cliente",
            description = "Autentica al cliente con credenciales y devuelve un JWT.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso. Devuelve JWT.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas (Email o contraseña).",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario deshabilitado.",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse authResponse = loginCustomerUseCase.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}