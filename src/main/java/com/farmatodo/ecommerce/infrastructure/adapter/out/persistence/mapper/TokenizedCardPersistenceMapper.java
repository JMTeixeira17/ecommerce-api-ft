package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.ecommerce.domain.model.TokenizedCard;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TokenizedCardEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface TokenizedCardPersistenceMapper {
    TokenizedCardEntity toEntity(TokenizedCard domain);
    TokenizedCard toDomain(TokenizedCardEntity entity);
}