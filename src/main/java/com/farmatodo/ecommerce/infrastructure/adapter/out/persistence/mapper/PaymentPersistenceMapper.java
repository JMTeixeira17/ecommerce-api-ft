package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.Order;
import com.farmatodo.ecommerce.domain.model.Payment;
import com.farmatodo.ecommerce.domain.model.TokenizedCard;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TokenizedCardEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentPersistenceMapper {

    Payment toDomain(PaymentEntity entity);
    Order toDomain(OrderEntity entity);
    TokenizedCard toDomain(TokenizedCardEntity entity);

    PaymentEntity toEntity(Payment domain);
}