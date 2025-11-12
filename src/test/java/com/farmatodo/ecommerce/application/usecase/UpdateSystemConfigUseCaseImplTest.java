package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.SystemConfigRequest;
import com.farmatodo.ecommerce.domain.exception.InvalidConfigValueException;
import com.farmatodo.ecommerce.domain.model.SystemConfig;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateSystemConfigUseCaseImplTest {

    @Mock
    private SystemConfigRepositoryPort systemConfigRepository;

    @InjectMocks
    private UpdateSystemConfigUseCaseImpl updateSystemConfigUseCase;

    private SystemConfig mockConfig;

    @BeforeEach
    void setUp() {
        mockConfig = SystemConfig.builder()
                .configKey("key")
                .configValue("value")
                .build();
    }
    private SystemConfigRequest createRequest(String key, String value) {
        SystemConfigRequest request = new SystemConfigRequest();
        request.setConfigKey(key);
        request.setConfigValue(value);
        return request;
    }
    @Test
    void whenUpdateConfig_withValidProbability_shouldSucceed() {
        String key = "tokenization.rejection.probability";
        SystemConfigRequest request = createRequest(key, "0.5");
        when(systemConfigRepository.updateValue(anyString(), anyString())).thenReturn(mockConfig);
        SystemConfig result = updateSystemConfigUseCase.updateConfig(request);
        assertNotNull(result);
        verify(systemConfigRepository, times(1)).updateValue(key, "0.5");
    }

    @Test
    void whenUpdateConfig_withValidInteger_shouldSucceed() {
        String key = "payment.max.retry.attempts";
        SystemConfigRequest request = createRequest(key, "5");
        when(systemConfigRepository.updateValue(anyString(), anyString())).thenReturn(mockConfig);
        SystemConfig result = updateSystemConfigUseCase.updateConfig(request);
        assertNotNull(result);
        verify(systemConfigRepository, times(1)).updateValue(key, "5");
    }

    @Test
    void whenUpdateConfig_withZeroForMinStockVisibility_shouldSucceed() {
        String key = "product.min.stock.visibility";
        SystemConfigRequest request = createRequest(key, "0");
        when(systemConfigRepository.updateValue(anyString(), anyString())).thenReturn(mockConfig);
        SystemConfig result = updateSystemConfigUseCase.updateConfig(request);
        assertNotNull(result);
        verify(systemConfigRepository, times(1)).updateValue(key, "0");
    }
    @Test
    void whenUpdateConfig_withUnauthorizedKey_shouldThrowInvalidConfigValueException() {
        String key = "unauthorized.key.secret";
        SystemConfigRequest request = createRequest(key, "some_value");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });
        assertTrue(exception.getMessage().contains("Clave de configuración no permitida"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }
    @Test
    void whenValidateProbability_withValueGreaterThanOne_shouldThrowException() {
        String key = "tokenization.rejection.probability";
        SystemConfigRequest request = createRequest(key, "1.01");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });

        assertTrue(exception.getMessage().contains("debe ser un número entre 0.0 y 1.0"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }

    @Test
    void whenValidateProbability_withValueLessThanZero_shouldThrowException() {
        String key = "payment.rejection.probability";
        SystemConfigRequest request = createRequest(key, "-0.01");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });
        assertTrue(exception.getMessage().contains("debe ser un número entre 0.0 y 1.0"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }

    @Test
    void whenValidateProbability_withNonNumericValue_shouldThrowException() {
        String key = "payment.rejection.probability";
        SystemConfigRequest request = createRequest(key, "not.a.number");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });
        assertTrue(exception.getMessage().contains("debe ser un número decimal válido"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }

    @Test
    void whenValidateInteger_withNonNumericValue_shouldThrowException() {
        String key = "payment.max.retry.attempts";
        SystemConfigRequest request = createRequest(key, "not an int");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });
        assertTrue(exception.getMessage().contains("debe ser un número entero válido"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }

    @Test
    void whenValidateInteger_withNegativeValue_shouldThrowException() {
        String key = "cart.expiration.hours";
        SystemConfigRequest request = createRequest(key, "-1");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });
        assertTrue(exception.getMessage().contains("debe ser un número entero positivo"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }

    @Test
    void whenValidateInteger_withZeroValueForNonMinStockKey_shouldThrowException() {
        String key = "email.max.retries";
        SystemConfigRequest request = createRequest(key, "0");
        Exception exception = assertThrows(InvalidConfigValueException.class, () -> {
            updateSystemConfigUseCase.updateConfig(request);
        });
        assertTrue(exception.getMessage().contains("debe ser un número entero positivo (o cero para min. stock)"));
        verify(systemConfigRepository, never()).updateValue(anyString(), anyString());
    }
}