package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.TokenizedCard;
import com.farmatodo.ecommerce.domain.port.out.TokenizedCardRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.TokenizedCardEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.TokenizedCardPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.TokenizedCardJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TokenizedCardPersistenceAdapter implements TokenizedCardRepositoryPort {

    private final TokenizedCardJpaRepository jpaRepository;
    private final TokenizedCardPersistenceMapper mapper;

    @Override
    public TokenizedCard save(TokenizedCard card) {
        TokenizedCardEntity entity = mapper.toEntity(card);
        TokenizedCardEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByCustomerIdAndLastFourDigitsAndCardBrand(
            Long customerId,
            String lastFourDigits,
            String cardBrand
    ) {
        return jpaRepository.existsByCustomerIdAndLastFourDigitsAndCardBrand(
                customerId,
                lastFourDigits,
                cardBrand
        );
    }
    @Override
    public List<TokenizedCard> findByCustomerIdAndIsActiveTrue(Long customerId) {
        return jpaRepository.findByCustomerIdAndIsActiveTrue(customerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TokenizedCard> findByUuidAndCustomerId(UUID uuid, Long customerId) {
        return jpaRepository.findByUuidAndCustomerId(uuid, customerId)
                .map(mapper::toDomain);
    }
}