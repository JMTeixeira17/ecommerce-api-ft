package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.ProductSearch;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductSearchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductSearchPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    ProductSearchEntity toEntity(ProductSearch domain);
}