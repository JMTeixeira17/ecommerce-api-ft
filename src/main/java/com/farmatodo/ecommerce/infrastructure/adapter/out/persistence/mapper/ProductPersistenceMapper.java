package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {


    Product toDomain(ProductEntity entity);
    List<Product> toDomainList(List<ProductEntity> entities);
    ProductEntity toEntity(Product product);
}