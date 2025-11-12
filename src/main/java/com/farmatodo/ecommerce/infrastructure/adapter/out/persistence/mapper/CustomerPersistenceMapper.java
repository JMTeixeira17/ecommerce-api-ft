package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerPersistenceMapper {
    CustomerEntity toEntity(Customer customer);
    Customer toDomain(CustomerEntity entity);
}