package com.farmatodo.ecommerce.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${security.api-key}")
    private String configuredApiKey;

    private static final String API_KEY_HEADER = "X-API-KEY";

    static final String TOKENIZE_PATH = "/tokenize";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getRequestURI().substring(request.getContextPath().length());

        if (!path.startsWith(TOKENIZE_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedApiKey = request.getHeader(API_KEY_HEADER);

        if (providedApiKey != null && providedApiKey.equals(configuredApiKey)) {
            PreAuthenticatedAuthenticationToken authentication =
                    new PreAuthenticatedAuthenticationToken(
                            "api-service-user",
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_SERVICE"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("APIKEY invalida. Por favor revise el formato.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}