package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.ProductSearch;
import com.farmatodo.ecommerce.domain.port.out.ProductSearchRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductSearchEntity;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.ProductSearchPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.ProductSearchJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSearchPersistenceAdapter implements ProductSearchRepositoryPort {

    private final ProductSearchJpaRepository jpaRepository;
    private final ProductSearchPersistenceMapper mapper;

    @Override
    public void save(ProductSearch productSearch) {
        ProductSearchEntity entity = mapper.toEntity(productSearch);
        jpaRepository.save(entity);
    }
}