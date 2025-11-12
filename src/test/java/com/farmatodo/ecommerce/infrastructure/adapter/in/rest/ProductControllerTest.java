package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.ProductResponse;
import com.farmatodo.ecommerce.domain.exception.SearchQueryException;
import com.farmatodo.ecommerce.domain.port.in.*;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SearchProductUseCase searchProductUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean public SearchProductUseCase searchProductUseCase() { return Mockito.mock(SearchProductUseCase.class); }
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
        @Bean public AddProductToCartUseCase apu() { return Mockito.mock(AddProductToCartUseCase.class); }
        @Bean public CreateOrderUseCase cou() { return Mockito.mock(CreateOrderUseCase.class); }
        @Bean public ProcessPaymentUseCase ppu() { return Mockito.mock(ProcessPaymentUseCase.class); }
        @Bean public CustomerRepositoryPort customerRepositoryPort() { return Mockito.mock(CustomerRepositoryPort.class); }
    }

    @Test
    @WithMockUser
    void whenSearchProducts_withValidQuery_shouldReturnProducts() throws Exception {
        ProductResponse mockResponse = ProductResponse.builder().name("Paracetamol").build();
        List<ProductResponse> responseList = Collections.singletonList(mockResponse);

        when(searchProductUseCase.searchProducts(anyString(), any(HttpServletRequest.class)))
                .thenReturn(responseList);
        mockMvc.perform(get("/products/search")
                        .param("q", "paracetamol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("Paracetamol"));
    }

    @Test
    @WithMockUser
    void whenSearchProducts_withInvalidQuery_shouldReturnBadRequest() throws Exception {

        when(searchProductUseCase.searchProducts(anyString(), any(HttpServletRequest.class)))
                .thenThrow(new SearchQueryException("La consulta de búsqueda debe tener al menos 3 caracteres."));

        mockMvc.perform(get("/products/search")
                        .param("q", "pa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error").value("La consulta de búsqueda debe tener al menos 3 caracteres."));
    }
}