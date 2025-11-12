package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.AddItemRequest;
import com.farmatodo.ecommerce.application.dto.CartResponse;
import com.farmatodo.ecommerce.domain.exception.InsufficientStockException;
import com.farmatodo.ecommerce.domain.exception.ProductNotFoundException;
import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import com.farmatodo.ecommerce.domain.port.in.AddProductToCartUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private AddProductToCartUseCase addProductToCartUseCase;

    @BeforeEach
    void setUp() {
        addProductToCartUseCase = Mockito.mock(AddProductToCartUseCase.class);

        CartController controller = new CartController(addProductToCartUseCase);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenAddItem_withSufficientStock_shouldReturnUpdatedCart() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        CartResponse mockResponse = CartResponse.builder()
                .totalItems(1)
                .totalAmount(new BigDecimal("100.00"))
                .status(ShoppingCart.CartStatus.ACTIVE)
                .items(Collections.emptyList())
                .build();

        when(addProductToCartUseCase.addProductToCart(any(AddItemRequest.class), any(HttpServletRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    void whenAddItem_withInsufficientStock_shouldReturnConflict() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(150);

        when(addProductToCartUseCase.addProductToCart(any(AddItemRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new InsufficientStockException("Stock insuficiente"));

        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Stock insuficiente"));
    }

    @Test
    void whenAddItem_asAnonymous_shouldReturnCartAndSessionHeader() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        CartResponse mockResponse = CartResponse.builder()
                .sessionId("new-session-id")
                .totalItems(1)
                .build();

        when(addProductToCartUseCase.addProductToCart(any(AddItemRequest.class), any(HttpServletRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Session-ID", "new-session-id"))
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    void whenAddItem_withProductNotFound_shouldReturnNotFound() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);

        when(addProductToCartUseCase.addProductToCart(any(AddItemRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new ProductNotFoundException("Producto 999 no encontrado."));
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.error").value("Producto 999 no encontrado."));
    }
}
