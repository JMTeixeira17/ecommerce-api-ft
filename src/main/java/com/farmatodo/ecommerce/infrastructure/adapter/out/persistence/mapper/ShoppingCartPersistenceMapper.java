package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.CartItem;
import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ShoppingCartEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShoppingCartPersistenceMapper {

    ShoppingCart toDomain(ShoppingCartEntity entity);
    CartItem toDomain(CartItemEntity entity);
    Product toDomain(ProductEntity entity);
    ShoppingCartEntity toEntity(ShoppingCart domain);
    CartItemEntity toEntity(CartItem domain);
}