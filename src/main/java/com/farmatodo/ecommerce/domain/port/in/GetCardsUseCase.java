package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.domain.model.Customer;

import java.util.List;

public interface GetCardsUseCase {
    List<RegisterCardResponse> getCustomerCards(Customer customer);
}