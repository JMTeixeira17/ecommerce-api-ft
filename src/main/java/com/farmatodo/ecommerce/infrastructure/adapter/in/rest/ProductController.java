package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.ProductResponse;
import com.farmatodo.ecommerce.domain.port.in.SearchProductUseCase;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Búsqueda y Catálogo de Productos")
public class ProductController {

    private final SearchProductUseCase searchProductUseCase;

    @Operation(summary = "Buscar productos",
            description = "Busca productos activos por nombre o descripción. Registra la consulta de forma asíncrona.")
    @Parameter(name = "q", description = "Término de búsqueda (mínimo 3 caracteres).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resultados de la búsqueda.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Consulta inválida (menos de 3 caracteres).")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam("q") String query,
            HttpServletRequest request) {
        List<ProductResponse> results = searchProductUseCase.searchProducts(query, request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}