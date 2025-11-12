package com.farmatodo.ecommerce.application.mapper;

import com.farmatodo.ecommerce.application.dto.PaymentResponse;
import com.farmatodo.ecommerce.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentApiMapper {

    @Mapping(source = "uuid", target = "paymentUuid")
    @Mapping(source = "order.uuid", target = "orderUuid")
    PaymentResponse toResponse(Payment payment);
}