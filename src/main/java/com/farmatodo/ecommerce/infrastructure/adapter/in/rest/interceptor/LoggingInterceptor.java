package com.farmatodo.ecommerce.infrastructure.adapter.in.rest.interceptor;

import com.farmatodo.ecommerce.application.event.LogApiEvent;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisher eventPublisher;
    private final CustomerRepositoryPort customerRepositoryPort;

    private static final int MAX_BODY_LENGTH = 2000;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (request.getRequestURI().equals("/error")) {
                return;
            }

            long startTime = (Long) request.getAttribute("startTime");
            long executionTime = System.currentTimeMillis() - startTime;

            String principalEmail = getPrincipalEmail();
            Long customerId = getCustomerIdFromEmail(principalEmail);

            String requestBody = getRequestBody(request);
            String responseBody = getResponseBody(response);

            LogApiEvent logEvent = LogApiEvent.builder()
                    .httpMethod(request.getMethod())
                    .endpoint(request.getRequestURI())
                    .statusCode(response.getStatus())
                    .ipAddress(request.getRemoteAddr())
                    .userAgent(request.getHeader("User-Agent"))
                    .executionTimeMs((int) executionTime)
                    .customerId(customerId)
                    .responseBody(truncate(responseBody, MAX_BODY_LENGTH))
                    .success(ex == null && response.getStatus() < 400)
                    .errorMessage(ex != null ? ex.getMessage() : null)
                    .stackTrace(ex != null ? getStackTrace(ex) : null)
                    .build();

            eventPublisher.publishEvent(logEvent);

        } catch (Exception e) {
            log.error("Error critical logging API request: {}", e.getMessage(), e);
        }
    }

    private String getPrincipalEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return null;
    }

    private Long getCustomerIdFromEmail(String email) {
        if (email == null) {
            return null;
        }
        try {
            return customerRepositoryPort.findByEmail(email).map(Customer::getId).orElse(null);
        } catch (Exception e) {
            log.warn("Could not retrieve customerId for log: {}", e.getMessage());
            return null;
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            return new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        }
        return "N/A (Request not cached)";
    }

    private String getResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            return new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        }
        return "N/A (Response not cached)";
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    private String truncate(String input, int length) {
        if (input == null) {
            return null;
        }
        if (input.length() <= length) {
            return input;
        }
        return input.substring(0, length) + "... [TRUNCATED]";
    }
}