package com.farmatodo.ecommerce.application.mapper;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.domain.model.TokenizedCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardApiMapper {

    @Mapping(source = "uuid", target = "cardId")
    RegisterCardResponse toResponse(TokenizedCard card);

    List<RegisterCardResponse> toResponseList(List<TokenizedCard> cards);
}