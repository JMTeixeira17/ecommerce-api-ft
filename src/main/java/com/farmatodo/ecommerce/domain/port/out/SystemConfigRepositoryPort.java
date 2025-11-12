package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.SystemConfig;

import java.util.Optional;

public interface SystemConfigRepositoryPort {

    Optional<String> getValue(String key);

    int getValueAsInt(String key, int defaultValue);

    double getValueAsDouble(String key, double defaultValue);

    SystemConfig updateValue(String key, String newValue);
}