package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.ProductSearch;

public interface ProductSearchRepositoryPort {


    void save(ProductSearch productSearch);
}