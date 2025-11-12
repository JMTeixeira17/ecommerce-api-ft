package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.LoginRequest;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.in.LoginCustomerUseCase;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginCustomerUseCaseImpl implements LoginCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String userEmail = authentication.getName();
        Customer customer = customerRepositoryPort.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en BD"));
        String jwtToken = jwtService.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
        return AuthResponse.builder()
                .token(jwtToken)
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .build();
    }
}