package com.farmatodo.ecommerce.application.mapper;

import com.farmatodo.ecommerce.application.dto.CartItemResponse;
import com.farmatodo.ecommerce.application.dto.CartResponse;
import com.farmatodo.ecommerce.domain.model.CartItem;
import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartApiMapper {

    @Mapping(source = "uuid", target = "cartUuid")
    CartResponse toResponse(ShoppingCart cart);

    @Mapping(source = "uuid", target = "itemUuid")
    @Mapping(source = "product.uuid", target = "productUuid")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.imageUrl", target = "productImageUrl")
    CartItemResponse toResponse(CartItem cartItem);
}