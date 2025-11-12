package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;

public interface TokenizeCardUseCase {
    TokenizeResponse tokenize(TokenizeRequest request);
}