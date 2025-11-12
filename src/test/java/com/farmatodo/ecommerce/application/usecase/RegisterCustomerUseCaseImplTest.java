package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.RegisterCustomerRequest;
import com.farmatodo.ecommerce.application.mapper.AuthApiMapper;
import com.farmatodo.ecommerce.domain.exception.CustomerAlreadyExistsException;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria pura para RegisterCustomerUseCaseImpl.
 * @ExtendWith(MockitoExtension.class) nos da mocks sin Spring.
 */
@ExtendWith(MockitoExtension.class)
class RegisterCustomerUseCaseImplTest {
    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthApiMapper authApiMapper;
    @Mock
    private UserDetailsService userDetailsService;
    @InjectMocks
    private RegisterCustomerUseCaseImpl registerCustomerUseCase;
    private RegisterCustomerRequest request;
    private Customer customer;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new RegisterCustomerRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setPhone("+123456789");

        customer = Customer.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword")
                .isActive(true)
                .build();

        userDetails = new User(customer.getEmail(), customer.getPassword(), Collections.emptyList());
    }

    @Test
    void whenRegister_withNewEmailAndPhone_shouldSucceedAndReturnToken() {
        when(customerRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(customerRepositoryPort.findByPhone(anyString())).thenReturn(Optional.empty());
        when(authApiMapper.toCustomer(any(RegisterCustomerRequest.class))).thenReturn(customer);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(customerRepositoryPort.save(any(Customer.class))).thenReturn(customer);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        AuthResponse response = registerCustomerUseCase.register(request);
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void whenRegister_withExistingEmail_shouldThrowCustomerAlreadyExistsException() {
        when(customerRepositoryPort.existsByEmail("test@example.com")).thenReturn(true);
        Exception exception = assertThrows(CustomerAlreadyExistsException.class, () -> {
            registerCustomerUseCase.register(request);
        });
        assertEquals("El email ya existe.", exception.getMessage());
    }

    @Test
    void whenRegister_withExistingPhone_shouldThrowCustomerAlreadyExistsException() {
        when(customerRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(customerRepositoryPort.findByPhone("+123456789")).thenReturn(Optional.of(customer));
        Exception exception = assertThrows(CustomerAlreadyExistsException.class, () -> {
            registerCustomerUseCase.register(request);
        });
        assertEquals("El telefono ya existe.", exception.getMessage());
    }
}