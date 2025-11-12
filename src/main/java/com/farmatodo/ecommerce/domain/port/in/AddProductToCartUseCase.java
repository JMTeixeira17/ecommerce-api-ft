package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.AddItemRequest;
import com.farmatodo.ecommerce.application.dto.CartResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AddProductToCartUseCase {

    CartResponse addProductToCart(AddItemRequest request, HttpServletRequest servletRequest);
}