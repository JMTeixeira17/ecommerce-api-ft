package com.farmatodo.ecommerce.application.mapper;

import com.farmatodo.ecommerce.application.dto.OrderItemResponse;
import com.farmatodo.ecommerce.application.dto.OrderResponse;
import com.farmatodo.ecommerce.domain.model.Order;
import com.farmatodo.ecommerce.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderApiMapper {

    @Mapping(source = "uuid", target = "orderUuid")
    OrderResponse toResponse(Order order);

    @Mapping(source = "uuid", target = "itemUuid")
    @Mapping(source = "product.uuid", target = "productUuid")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    OrderItemResponse toResponse(OrderItem item);
}