package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.TokenizedCard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenizedCardRepositoryPort {

    TokenizedCard save(TokenizedCard card);

    boolean existsByCustomerIdAndLastFourDigitsAndCardBrand(
            Long customerId,
            String lastFourDigits,
            String cardBrand
    );

    List<TokenizedCard> findByCustomerIdAndIsActiveTrue(Long customerId);
    Optional<TokenizedCard> findByUuidAndCustomerId(UUID uuid, Long customerId);
}