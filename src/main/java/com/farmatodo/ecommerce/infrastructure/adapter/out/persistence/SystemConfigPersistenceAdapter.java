package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.exception.SystemConfigNotFoundException;
import com.farmatodo.ecommerce.domain.model.SystemConfig;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.SystemConfigEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.SystemConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SystemConfigPersistenceAdapter implements SystemConfigRepositoryPort {

    private final SystemConfigJpaRepository jpaRepository;

    @Override
    public Optional<String> getValue(String key) {
        return jpaRepository.findByConfigKeyAndIsActiveTrue(key)
                .map(SystemConfigEntity::getConfigValue);
    }

    @Override
    public int getValueAsInt(String key, int defaultValue) {
        try {
            return getValue(key)
                    .map(configValue -> {
                        try {
                            return Integer.parseInt(configValue);
                        } catch (NumberFormatException e) {
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double getValueAsDouble(String key, double defaultValue) {
        try {
            return getValue(key)
                    .map(configValue -> {
                        try {
                            return Double.parseDouble(configValue);
                        } catch (NumberFormatException e) {
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public SystemConfig updateValue(String key, String newValue) {
        SystemConfigEntity entity = jpaRepository.findByConfigKeyAndIsActiveTrue(key)
                .orElseThrow(() -> new SystemConfigNotFoundException("Configuración no encontrada: " + key));

        entity.setConfigValue(newValue);

        return toDomain(jpaRepository.save(entity));
    }

    private SystemConfig toDomain(SystemConfigEntity entity) {
        return SystemConfig.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getConfigValue())
                .description(entity.getDescription())
                .build();
    }
}