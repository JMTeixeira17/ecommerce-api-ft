package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.Order;
import com.farmatodo.ecommerce.domain.model.OrderItem;
import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.OrderItemEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderPersistenceMapper {

    Order toDomain(OrderEntity entity);
    OrderItem toDomain(OrderItemEntity entity);
    Customer toDomain(CustomerEntity entity);
    Product toDomain(ProductEntity entity);
    OrderEntity toEntity(Order domain);
    OrderItemEntity toEntity(OrderItem domain);
}