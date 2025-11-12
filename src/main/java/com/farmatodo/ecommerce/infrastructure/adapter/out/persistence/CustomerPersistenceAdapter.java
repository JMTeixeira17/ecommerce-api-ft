package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence;

import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.mapper.CustomerPersistenceMapper;
import com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = mapper.toEntity(customer);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}