package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.AddItemRequest;
import com.farmatodo.ecommerce.application.dto.CartResponse;
import com.farmatodo.ecommerce.application.usecase.AddProductToCartUseCaseImpl;
import com.farmatodo.ecommerce.domain.port.in.AddProductToCartUseCase;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Carro de productos", description = "Gestión del Carrito de Compras (Anónimo/Autenticado)")
public class CartController {

    private final AddProductToCartUseCase addProductToCartUseCase;

    @Operation(summary = "Añadir/Actualizar producto en el carrito",
            description = "Crea un carrito anónimo (si es la primera vez) o añade/actualiza la cantidad de un producto. El Session ID se maneja vía header 'X-Session-ID'.")
    @Parameter(in = ParameterIn.HEADER, name = AddProductToCartUseCaseImpl.SESSION_ID_HEADER, description = "ID de sesión anónima (se devuelve si es nuevo).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto añadido/actualizado. Devuelve el estado actual del carrito y el X-Session-ID en el header de respuesta.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado.",
            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock insuficiente.",
            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)))
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addProductToCart(
            @Valid @RequestBody AddItemRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        CartResponse cartResponse = addProductToCartUseCase.addProductToCart(request, servletRequest);

        if (cartResponse.getSessionId() != null) {
            servletResponse.addHeader(AddProductToCartUseCaseImpl.SESSION_ID_HEADER, cartResponse.getSessionId());
        }

        return ResponseEntity.ok(ApiResponse.success(cartResponse));
    }
}