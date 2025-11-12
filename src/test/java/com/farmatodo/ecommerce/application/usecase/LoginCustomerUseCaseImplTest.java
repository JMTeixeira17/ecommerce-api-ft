package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.AuthResponse;
import com.farmatodo.ecommerce.application.dto.LoginRequest;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginCustomerUseCaseImplTest {
    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private LoginCustomerUseCaseImpl loginCustomerUseCase;
    private LoginRequest request;
    private Customer customer;
    private Authentication authentication;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        customer = Customer.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .build();
        userDetails = new User(customer.getEmail(), "hashedPassword", Collections.emptyList());
        authentication = mock(Authentication.class);
    }

    @Test
    void whenLogin_withValidCredentials_shouldSucceed() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(customerRepositoryPort.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        AuthResponse response = loginCustomerUseCase.login(request);
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Test", response.getFirstName());
    }

    @Test
    void whenLogin_withInvalidCredentials_shouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Bad credentials"));
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            loginCustomerUseCase.login(request);
        });
        assertEquals("Bad credentials", exception.getMessage());
    }
}