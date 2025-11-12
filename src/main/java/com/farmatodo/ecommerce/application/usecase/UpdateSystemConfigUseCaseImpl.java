package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.SystemConfigRequest;
import com.farmatodo.ecommerce.domain.exception.InvalidConfigValueException;
import com.farmatodo.ecommerce.domain.model.SystemConfig;
import com.farmatodo.ecommerce.domain.port.in.UpdateSystemConfigUseCase;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateSystemConfigUseCaseImpl implements UpdateSystemConfigUseCase {

    private final SystemConfigRepositoryPort systemConfigRepository;

    private static final Set<String> ALLOWED_CONFIGS = Set.of(
            "tokenization.rejection.probability",
            "payment.rejection.probability",
            "payment.max.retry.attempts",
            "product.min.stock.visibility",
            "cart.expiration.hours",
            "email.max.retries"
    );

    @Override
    @Transactional
    public SystemConfig updateConfig(SystemConfigRequest request) {
        String key = request.getConfigKey();
        String value = request.getConfigValue();
        if (!ALLOWED_CONFIGS.contains(key)) {
            throw new InvalidConfigValueException("Clave de configuración no permitida para modificación: " + key);
        }
        if (key.endsWith(".probability")) {
            validateProbability(value, key);
        } else if (key.endsWith(".attempts") || key.endsWith(".retries") || key.endsWith(".hours") || key.endsWith(".visibility")) {
            validateInteger(value, key);
        }
        log.info("Actualizando configuración: {} con valor: {}", key, value);
        return systemConfigRepository.updateValue(key, value);
    }
    private void validateProbability(String value, String key) {
        try {
            double prob = Double.parseDouble(value);
            if (prob < 0.0 || prob > 1.0) {
                throw new InvalidConfigValueException(
                        String.format("El valor para la probabilidad de rechazo ('%s') debe ser un número entre 0.0 y 1.0.", key)
                );
            }
        } catch (NumberFormatException e) {
            throw new InvalidConfigValueException(
                    String.format("El valor para '%s' debe ser un número decimal válido (ej. 0.15).", key)
            );
        }
    }
    private void validateInteger(String value, String key) {
        try {
            int intValue = Integer.parseInt(value);
            if (intValue < 0 || (intValue == 0 && !key.equals("product.min.stock.visibility"))) {
                throw new InvalidConfigValueException(
                        String.format("El valor para '%s' debe ser un número entero positivo (o cero para min. stock).", key)
                );
            }
        } catch (NumberFormatException e) {
            throw new InvalidConfigValueException(
                    String.format("El valor para '%s' debe ser un número entero válido.", key)
            );
        }
    }
}