package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.LoginRequest;
import com.farmatodo.ecommerce.application.dto.RegisterCustomerRequest;
import com.farmatodo.ecommerce.domain.exception.CustomerAlreadyExistsException;
import com.farmatodo.ecommerce.domain.port.in.*;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.JwtService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.farmatodo.ecommerce.infrastructure.security.ApiKeyAuthFilter;
import com.farmatodo.ecommerce.infrastructure.security.CustomAccessDeniedHandler;
import com.farmatodo.ecommerce.infrastructure.security.CustomAuthenticationEntryPoint;
import com.farmatodo.ecommerce.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationProvider;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegisterCustomerUseCase registerCustomerUseCase;
    @Autowired
    private LoginCustomerUseCase loginCustomerUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean public RegisterCustomerUseCase registerCustomerUseCase() { return Mockito.mock(RegisterCustomerUseCase.class); }
        @Bean public LoginCustomerUseCase loginCustomerUseCase() { return Mockito.mock(LoginCustomerUseCase.class); }
        @Bean public JwtService jwtService() { return Mockito.mock(JwtService.class); }
        @Bean public UserDetailsService userDetailsService() { return Mockito.mock(UserDetailsService.class); }
        @Bean public JwtAuthFilter jwtAuthFilter() {
            return Mockito.mock(JwtAuthFilter.class);
        }
        @Bean public ApiKeyAuthFilter apiKeyAuthFilter() { return Mockito.mock(ApiKeyAuthFilter.class); }
        @Bean public AuthenticationProvider authenticationProvider() { return Mockito.mock(AuthenticationProvider.class); }
        @Bean public CustomAccessDeniedHandler customAccessDeniedHandler() { return Mockito.mock(CustomAccessDeniedHandler.class); }
        @Bean public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() { return Mockito.mock(CustomAuthenticationEntryPoint.class); }
        @Bean public TokenizeCardUseCase tokenizeCardUseCase() { return Mockito.mock(TokenizeCardUseCase.class); }
        @Bean public RegisterCardUseCase registerCardUseCase() { return Mockito.mock(RegisterCardUseCase.class); }
        @Bean public SearchProductUseCase searchProductUseCase() { return Mockito.mock(SearchProductUseCase.class); }
        @Bean public AddProductToCartUseCase addProductToCartUseCase() { return Mockito.mock(AddProductToCartUseCase.class); }
        @Bean public CreateOrderUseCase createOrderUseCase() { return Mockito.mock(CreateOrderUseCase.class); }
        @Bean public ProcessPaymentUseCase processPaymentUseCase() { return Mockito.mock(ProcessPaymentUseCase.class); }

        @Bean public CustomerRepositoryPort customerRepositoryPort() { return Mockito.mock(CustomerRepositoryPort.class); }
    }


    @Test
    void whenRegister_withValidData_shouldReturnToken() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhone("+582122258554");
        request.setAddressLine1("123 Main St");
        request.setCity("Testville");
        request.setState("TS");
        request.setPostalCode("12345");
        request.setCountry("MX");

        AuthResponse authResponse = AuthResponse.builder()
                .token("mock-jwt-token")
                .email("test@example.com")
                .build();

        when(registerCustomerUseCase.register(any(RegisterCustomerRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"));
    }

    @Test
    void whenRegister_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("not-an-email");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhone("+582122258554");
        request.setAddressLine1("123 Main St");
        request.setCity("Testville");
        request.setState("TS");
        request.setPostalCode("12345");
        request.setCountry("MX");

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void whenRegister_withExistingEmail_shouldReturnConflict() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhone("+582122258554");
        request.setAddressLine1("123 Main St");
        request.setCity("Testville");
        request.setState("TS");
        request.setPostalCode("12345");
        request.setCountry("MX");


        when(registerCustomerUseCase.register(any(RegisterCustomerRequest.class)))
                .thenThrow(new CustomerAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void whenLogin_withValidCredentials_shouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        AuthResponse authResponse = AuthResponse.builder().token("mock-jwt-token").build();
        when(loginCustomerUseCase.login(any(LoginRequest.class))).thenReturn(authResponse);
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"));
    }

    @Test
    void whenLogin_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(loginCustomerUseCase.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Usuario o contraseña inválido."));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.error").value("Usuario o contraseña inválido."));
    }
}