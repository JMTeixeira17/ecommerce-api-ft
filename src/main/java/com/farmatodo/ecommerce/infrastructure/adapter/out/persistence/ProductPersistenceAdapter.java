package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.domain.port.out.ProductRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.ProductPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    @Override
    public List<Product> findByNameContainingIgnoreCaseAndStockGreaterThan(String name, int minStock) {
        var entities = jpaRepository.findByNameContainingIgnoreCaseAndStockGreaterThanAndIsActiveTrue(name, minStock);
        return mapper.toDomainList(entities);
    }

    @Override
    public Optional<Product> findByIdAndIsActiveTrue(Long id) {
        return jpaRepository.findByIdAndIsActiveTrue(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdForUpdate(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void saveAll(List<Product> products) {
        var entities = products.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }
}