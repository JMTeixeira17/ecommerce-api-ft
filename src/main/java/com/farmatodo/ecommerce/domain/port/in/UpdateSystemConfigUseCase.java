package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.SystemConfigRequest;
import com.farmatodo.ecommerce.domain.model.SystemConfig;

public interface UpdateSystemConfigUseCase {

    SystemConfig updateConfig(SystemConfigRequest request);
}