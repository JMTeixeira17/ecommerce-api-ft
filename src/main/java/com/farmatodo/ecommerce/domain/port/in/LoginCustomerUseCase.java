package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.LoginRequest;

public interface LoginCustomerUseCase {
    AuthResponse login(LoginRequest request);
}