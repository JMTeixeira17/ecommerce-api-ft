package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.Product;
import java.util.List;
import java.util.Optional;


public interface ProductRepositoryPort {

    List<Product> findByNameContainingIgnoreCaseAndStockGreaterThan(String name, int minStock);

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    void saveAll(List<Product> products);

    Optional<Product> findByIdForUpdate(Long id);


}