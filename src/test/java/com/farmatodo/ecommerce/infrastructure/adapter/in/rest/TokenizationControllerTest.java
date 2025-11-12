package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;
import com.farmatodo.ecommerce.domain.exception.InvalidCardDataException;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TokenizationController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class TokenizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenizeCardUseCase tokenizeCardUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean public TokenizeCardUseCase tokenizeCardUseCase() { return Mockito.mock(TokenizeCardUseCase.class); }
        @Bean public RegisterCustomerUseCase registerCustomerUseCase() { return Mockito.mock(RegisterCustomerUseCase.class); }
        @Bean public LoginCustomerUseCase loginCustomerUseCase() { return Mockito.mock(LoginCustomerUseCase.class); }
        @Bean public JwtService jwtService() { return Mockito.mock(JwtService.class); }
        @Bean public UserDetailsService userDetailsService() { return Mockito.mock(UserDetailsService.class); }
        @Bean public JwtAuthFilter jwtAuthFilter(JwtService jwt, UserDetailsService uds) { return new JwtAuthFilter(jwt, uds); }
        @Bean public ApiKeyAuthFilter apiKeyAuthFilter() { return Mockito.mock(ApiKeyAuthFilter.class); }
        @Bean public AuthenticationProvider authenticationProvider() { return Mockito.mock(AuthenticationProvider.class); }
        @Bean public CustomAccessDeniedHandler customAccessDeniedHandler() { return Mockito.mock(CustomAccessDeniedHandler.class); }
        @Bean public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() { return Mockito.mock(CustomAuthenticationEntryPoint.class); }
        @Bean public RegisterCardUseCase registerCardUseCase() { return Mockito.mock(RegisterCardUseCase.class); }
        @Bean public SearchProductUseCase searchProductUseCase() { return Mockito.mock(SearchProductUseCase.class); }
        @Bean public AddProductToCartUseCase addProductToCartUseCase() { return Mockito.mock(AddProductToCartUseCase.class); }
        @Bean public CreateOrderUseCase createOrderUseCase() { return Mockito.mock(CreateOrderUseCase.class); }
        @Bean public ProcessPaymentUseCase processPaymentUseCase() { return Mockito.mock(ProcessPaymentUseCase.class); }
        @Bean public CustomerRepositoryPort customerRepositoryPort() { return Mockito.mock(CustomerRepositoryPort.class); }
    }

    @Test
    void whenTokenize_withValidData_shouldReturnToken() throws Exception {
        TokenizeRequest request = new TokenizeRequest();
        request.setCardNumber("4242424242424242");
        request.setCvv("123");
        request.setExpirationMonth("12");
        request.setExpirationYear("2029");
        request.setCardHolderName("Test User");

        TokenizeResponse response = TokenizeResponse.builder()
                .token("tkn_123")
                .cardBrand("VISA")
                .lastFourDigits("4242")
                .build();

        when(tokenizeCardUseCase.tokenize(any(TokenizeRequest.class))).thenReturn(response);
        mockMvc.perform(post("/tokenize")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").value("tkn_123"));
    }

    @Test
    void whenTokenize_withInvalidData_shouldReturnBadRequest() throws Exception {
        TokenizeRequest request = new TokenizeRequest();
        request.setCardNumber("123");
        request.setCvv("123");
        request.setExpirationMonth("12");
        request.setExpirationYear("2029");
        request.setCardHolderName("Test User");
        when(tokenizeCardUseCase.tokenize(any(TokenizeRequest.class)))
                .thenThrow(new InvalidCardDataException("Numero de tarjeta inválido"));
        mockMvc.perform(post("/tokenize")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error").value("cardNumber: Numero de tarjeta inválido"));
    }
}