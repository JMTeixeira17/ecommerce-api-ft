package com.farmatodo.ecommerce.infrastructure.security;

import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return new User(
                customer.getEmail(),
                customer.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}