package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.domain.model.Customer;

public interface RegisterCardUseCase {

    RegisterCardResponse registerCard(TokenizeRequest request, Customer customer, String apiKey);
}