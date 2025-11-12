package com.farmatodo.ecommerce.application.mapper;

import com.farmatodo.ecommerce.application.dto.ProductResponse;
import com.farmatodo.ecommerce.domain.model.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {

    ProductResponse toResponse(Product product);
    List<ProductResponse> toResponseList(List<Product> products);
}