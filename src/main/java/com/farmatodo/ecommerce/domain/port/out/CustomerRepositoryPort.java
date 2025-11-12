package com.farmatodo.ecommerce.domain.port.out;

import com.farmatodo.ecommerce.domain.model.Customer;
import java.util.Optional;

public interface CustomerRepositoryPort {
    Customer save(Customer customer);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
    boolean existsByEmail(String email);
    Optional<Customer> findById(Long id);
}