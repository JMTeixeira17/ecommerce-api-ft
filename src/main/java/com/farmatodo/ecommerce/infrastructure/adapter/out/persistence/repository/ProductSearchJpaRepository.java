package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.ProductSearchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSearchJpaRepository extends JpaRepository<ProductSearchEntity, Long> {
}