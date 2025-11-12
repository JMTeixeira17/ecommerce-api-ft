package com.farmatodo.ecommerce.application.mapper;
import com.farmatodo.ecommerce.application.dto.RegisterCustomerRequest;
import com.farmatodo.ecommerce.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toCustomer(RegisterCustomerRequest request);
}