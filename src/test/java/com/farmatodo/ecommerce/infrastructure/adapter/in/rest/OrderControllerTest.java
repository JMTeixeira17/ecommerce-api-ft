package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.CreateOrderRequest;
import com.farmatodo.ecommerce.application.dto.OrderResponse;
import com.farmatodo.ecommerce.application.dto.PaymentResponse;
import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.domain.exception.CartNotFoundException;
import com.farmatodo.ecommerce.domain.exception.InsufficientStockException;
import com.farmatodo.ecommerce.domain.exception.PaymentException;
import com.farmatodo.ecommerce.domain.model.Order;
import com.farmatodo.ecommerce.domain.model.Payment;
import com.farmatodo.ecommerce.domain.port.in.*;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CreateOrderUseCase createOrderUseCase;
    @Autowired
    private ProcessPaymentUseCase processPaymentUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean public CreateOrderUseCase createOrderUseCase() { return Mockito.mock(CreateOrderUseCase.class); }
        @Bean public ProcessPaymentUseCase processPaymentUseCase() { return Mockito.mock(ProcessPaymentUseCase.class); }
        @Bean public RegisterCustomerUseCase rcu() { return Mockito.mock(RegisterCustomerUseCase.class); }
        @Bean public LoginCustomerUseCase lcu() { return Mockito.mock(LoginCustomerUseCase.class); }
        @Bean public JwtService jwtService() { return Mockito.mock(JwtService.class); }
        @Bean public UserDetailsService userDetailsService() { return Mockito.mock(UserDetailsService.class); }
        @Bean public JwtAuthFilter jwtAuthFilter(JwtService j, UserDetailsService u) { return new JwtAuthFilter(j, u); }
        @Bean public ApiKeyAuthFilter apiKeyAuthFilter() { return Mockito.mock(ApiKeyAuthFilter.class); }
        @Bean public AuthenticationProvider authenticationProvider() { return Mockito.mock(AuthenticationProvider.class); }
        @Bean public CustomAccessDeniedHandler c1() { return Mockito.mock(CustomAccessDeniedHandler.class); }
        @Bean public CustomAuthenticationEntryPoint c2() { return Mockito.mock(CustomAuthenticationEntryPoint.class); }
        @Bean public TokenizeCardUseCase tcu() { return Mockito.mock(TokenizeCardUseCase.class); }
        @Bean public RegisterCardUseCase rpu() { return Mockito.mock(RegisterCardUseCase.class); }
        @Bean public SearchProductUseCase spu() { return Mockito.mock(SearchProductUseCase.class); }
        @Bean public AddProductToCartUseCase apu() { return Mockito.mock(AddProductToCartUseCase.class); }
        @Bean public CustomerRepositoryPort customerRepositoryPort() { return Mockito.mock(CustomerRepositoryPort.class); }
    }

    @Test
    @WithMockUser
    void whenCreateOrder_withValidCart_shouldReturnCreated() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setTokenizedCardUuid(UUID.randomUUID().toString());
        request.setShippingAddressLine1("123 Main St");
        request.setShippingCity("City");
        request.setShippingState("State");
        request.setShippingPostalCode("12345");

        OrderResponse mockResponse = OrderResponse.builder()
                .orderNumber("ORD-123")
                .status(Order.OrderStatus.PENDING)
                .build();

        when(createOrderUseCase.createOrderFromCart(any(CreateOrderRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-123"));
    }

    @Test
    @WithMockUser
    void whenCreateOrder_withEmptyCart_shouldReturnNotFound() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setTokenizedCardUuid(UUID.randomUUID().toString());
        request.setShippingAddressLine1("123 Main St");
        request.setShippingCity("City");
        request.setShippingState("State");
        request.setShippingPostalCode("12345");


        when(createOrderUseCase.createOrderFromCart(any(CreateOrderRequest.class)))
                .thenThrow(new CartNotFoundException("Carrito vacío"));

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Carrito vacío"));
    }

    @Test
    @WithMockUser
    void whenProcessPayment_isApproved_shouldReturnOK() throws Exception {
        String orderUuid = UUID.randomUUID().toString();
        PaymentResponse mockResponse = PaymentResponse.builder()
                .status(Payment.PaymentStatus.APPROVED)
                .orderUuid(UUID.fromString(orderUuid))
                .build();

        when(processPaymentUseCase.processOrderPayment(orderUuid))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/orders/{orderUuid}/pay", orderUuid)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser
    void whenProcessPayment_isDeclined_shouldReturnUnprocessableEntity() throws Exception {
        String orderUuid = UUID.randomUUID().toString();

        when(processPaymentUseCase.processOrderPayment(orderUuid))
                .thenThrow(new PaymentException("Pago DECLINED. (Intento 2/3)."));
        mockMvc.perform(post("/orders/{orderUuid}/pay", orderUuid)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Pago DECLINED. (Intento 2/3)."));
    }
}