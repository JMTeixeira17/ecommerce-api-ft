package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.SystemConfigRequest;
import com.farmatodo.ecommerce.application.usecase.UpdateSystemConfigUseCaseImpl;
import com.farmatodo.ecommerce.domain.exception.InvalidConfigValueException;
import com.farmatodo.ecommerce.domain.model.SystemConfig;
import com.farmatodo.ecommerce.domain.port.in.*;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemConfigController.class)
@Import(SystemConfigControllerTest.MockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class SystemConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UpdateSystemConfigUseCaseImpl updateSystemConfigUseCase;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UpdateSystemConfigUseCaseImpl updateSystemConfigUseCase() {
            return Mockito.mock(UpdateSystemConfigUseCaseImpl.class);
        }
        @Bean
        public CustomerRepositoryPort customerRepositoryPort() {
            return Mockito.mock(CustomerRepositoryPort.class);
        }
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
    }

    private SystemConfigRequest createRequest(String key, String value) {
        SystemConfigRequest request = new SystemConfigRequest();
        request.setConfigKey(key);
        request.setConfigValue(value);
        return request;
    }

    private SystemConfig createMockConfig(String key, String value) {
        return SystemConfig.builder()
                .id(1L)
                .configKey(key)
                .configValue(value)
                .build();
    }

    @Test
    void whenUpdateConfig_withValidData_shouldReturnOk() throws Exception {
        String key = "payment.rejection.probability";
        SystemConfigRequest request = createRequest(key, "0.5");
        SystemConfig mockConfig = createMockConfig(key, "0.5");
        when(updateSystemConfigUseCase.updateConfig(any(SystemConfigRequest.class))).thenReturn(mockConfig);
        mockMvc.perform(put("/config")
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value(key))
                .andExpect(jsonPath("$.configValue").value("0.5"));
    }

    @Test
    void whenUpdateConfig_withInvalidValue_shouldReturnBadRequest() throws Exception {
        String key = "payment.rejection.probability";
        SystemConfigRequest request = createRequest(key, "2.0");
        when(updateSystemConfigUseCase.updateConfig(any(SystemConfigRequest.class)))
                .thenThrow(new InvalidConfigValueException("El valor debe ser entre 0.0 y 1.0."));
        mockMvc.perform(put("/config")
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El valor debe ser entre 0.0 y 1.0."));
    }

    @Test
    void whenUpdateConfig_withEmptyKey_shouldReturnBadRequest() throws Exception {
        SystemConfigRequest request = createRequest("", "0.1");
        mockMvc.perform(put("/config")
                        .with(anonymous())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}