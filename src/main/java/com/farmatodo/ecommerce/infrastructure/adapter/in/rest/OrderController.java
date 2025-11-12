package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.CreateOrderRequest;
import com.farmatodo.ecommerce.application.dto.OrderResponse;
import com.farmatodo.ecommerce.application.dto.PaymentResponse;
import com.farmatodo.ecommerce.domain.port.in.CreateOrderUseCase;
import com.farmatodo.ecommerce.domain.port.in.ProcessPaymentUseCase;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Ordenes y Pagos", description = "Gestión del Checkout y Procesamiento de Pagos")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;

    @Operation(summary = "Crear una orden (Checkout)",
            description = "Convierte el carrito activo del usuario a una orden PENDING, actualiza el stock y crea el registro de pago. Requiere JWT.")
    @SecurityRequirement(name = "BearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Orden creada exitosamente. Estado: PENDING.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de envío o tarjeta inválidos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Carrito no encontrado o vacío.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock insuficiente.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse orderResponse = createOrderUseCase.createOrderFromCart(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderResponse, HttpStatus.CREATED.value()));
    }

    @PostMapping("/{orderUuid}/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable String orderUuid) {
        PaymentResponse paymentResponse = processPaymentUseCase.processOrderPayment(orderUuid);
        return ResponseEntity.ok(ApiResponse.success(paymentResponse));
    }
}