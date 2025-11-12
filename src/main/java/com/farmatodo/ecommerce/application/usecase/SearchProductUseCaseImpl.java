package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.ProductResponse;
import com.farmatodo.ecommerce.application.event.ProductSearchEvent;
import com.farmatodo.ecommerce.application.mapper.ProductApiMapper;
import com.farmatodo.ecommerce.domain.exception.SearchQueryException;
import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.domain.port.in.SearchProductUseCase;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.ProductRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchProductUseCaseImpl implements SearchProductUseCase {

    private static final String MIN_STOCK_KEY = "product.min.stock.visibility";
    private static final int DEFAULT_MIN_STOCK = 5;

    private final ProductRepositoryPort productRepository;
    private final SystemConfigRepositoryPort systemConfigRepository;
    private final ProductApiMapper productApiMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query, HttpServletRequest request) {
        if (query == null || query.trim().length() < 3) {
            throw new SearchQueryException("La consulta de búsqueda debe tener al menos 3 caracteres.");
        }

        int minStock = systemConfigRepository.getValueAsInt(MIN_STOCK_KEY, DEFAULT_MIN_STOCK);

        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndStockGreaterThan(
                query,
                minStock
        );

        List<ProductResponse> productResponses = productApiMapper.toResponseList(products);
        try {
            Long customerId = getAuthenticatedCustomerId();

            ProductSearchEvent event = new ProductSearchEvent(
                    this,
                    customerId,
                    query,
                    productResponses.size(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Error al publicar ProductSearchEvent (pero la búsqueda fue exitosa): {}", e.getMessage());
        }

        return productResponses;
    }

    private Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        log.info("DEPURACIÓN: Validando usuario: {}", principal);
        log.info("DEPURACIÓN: {}", authentication);
        if (principal instanceof User) {
            String email = ((User) principal).getUsername();
            return customerRepositoryPort.findByEmail(email)
                    .map(customer -> customer.getId())
                    .orElse(null);
        }
        return null;
    }
}