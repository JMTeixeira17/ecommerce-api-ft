package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.ProductResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


public interface SearchProductUseCase {

    List<ProductResponse> searchProducts(String query, HttpServletRequest request);
}