package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.RegisterCustomerRequest;
import com.farmatodo.ecommerce.application.mapper.AuthApiMapper;
import com.farmatodo.ecommerce.domain.exception.CustomerAlreadyExistsException;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.in.RegisterCustomerUseCase;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class RegisterCustomerUseCaseImpl implements RegisterCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthApiMapper authApiMapper;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterCustomerRequest request) {
        if (customerRepositoryPort.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("El email ya existe.");
        }
        if (customerRepositoryPort.findByPhone(request.getPhone()).isPresent()) {
            throw new CustomerAlreadyExistsException("El telefono ya existe.");
        }

        Customer customer = authApiMapper.toCustomer(request);

        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setActive(true);

        Customer savedCustomer = customerRepositoryPort.save(customer);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedCustomer.getEmail());

        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedCustomer.getEmail())
                .firstName(savedCustomer.getFirstName())
                .build();
    }
}