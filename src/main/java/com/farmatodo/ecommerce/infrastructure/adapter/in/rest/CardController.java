package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.in.GetCardsUseCase;
import com.farmatodo.ecommerce.domain.port.in.RegisterCardUseCase;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Tokenización", description = "Endpoints de registro de tarjetas.")
public class CardController {

    private final RegisterCardUseCase registerCardUseCase;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final GetCardsUseCase getCardsUseCase;

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Operation(summary = "Registrar y tokenizar una tarjeta de crédito",
            description = "Endpoint de usuario. Llama internamente a /tokenize. Requiere un JWT válido y el header X-API-KEY.")
    @SecurityRequirement(name = "BearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tarjeta tokenizada y guardada exitosamente.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de tarjeta o formato inválidos.",
            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT inválido o ausente.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tarjeta ya registrada para este usuario.",
            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Tokenización rechazada por probabilidad (simulado).",
            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)))
    @PostMapping
    public ResponseEntity<ApiResponse<RegisterCardResponse>> registerCard(
            @Valid @RequestBody TokenizeRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest
    ) {
        Customer customer = customerRepositoryPort.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no existe"));

        String apiKey = servletRequest.getHeader(API_KEY_HEADER);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Se requiere un APIKEY.");
        }

        RegisterCardResponse response = registerCardUseCase.registerCard(request, customer, apiKey);

        return new ResponseEntity<>(ApiResponse.success(response, HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar tarjetas registradas del cliente",
            description = "Devuelve una lista de la metadata de las tarjetas tokenizadas activas para el usuario autenticado.")
    @SecurityRequirement(name = "BearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de tarjetas del cliente.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT inválido o ausente.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegisterCardResponse>>> getCards(
            Authentication authentication) {

        Customer customer = customerRepositoryPort.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no existe"));

        List<RegisterCardResponse> cards = getCardsUseCase.getCustomerCards(customer);

        return ResponseEntity.ok(ApiResponse.success(cards));
    }
}