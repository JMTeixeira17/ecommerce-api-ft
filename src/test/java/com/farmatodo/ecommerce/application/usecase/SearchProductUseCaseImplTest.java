package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.ProductResponse;
import com.farmatodo.ecommerce.application.event.ProductSearchEvent;
import com.farmatodo.ecommerce.application.mapper.ProductApiMapper;
import com.farmatodo.ecommerce.domain.exception.SearchQueryException;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.ProductRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private SystemConfigRepositoryPort systemConfigRepository;
    @Mock
    private ProductApiMapper productApiMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    @InjectMocks
    private SearchProductUseCaseImpl searchProductUseCase;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @Test
    void whenSearch_withValidQuery_andAnonymousUser_shouldReturnProductsAndLogEvent() {
        String query = "paracetamol";
        int minStock = 5;
        List<Product> mockProducts = Collections.singletonList(Product.builder().id(1L).name("Paracetamol").build());
        List<ProductResponse> mockResponses = Collections.singletonList(ProductResponse.builder().sku("123").name("Paracetamol").build());

        when(systemConfigRepository.getValueAsInt("product.min.stock.visibility", 5)).thenReturn(minStock);
        when(productRepository.findByNameContainingIgnoreCaseAndStockGreaterThan(query, minStock))
                .thenReturn(mockProducts);
        when(productApiMapper.toResponseList(mockProducts)).thenReturn(mockResponses);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);
        List<ProductResponse> results = searchProductUseCase.searchProducts(query, request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Paracetamol", results.get(0).getName());
        verify(eventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof ProductSearchEvent &&
                        ((ProductSearchEvent) event).getCustomerId() == null &&
                        ((ProductSearchEvent) event).getSearchQuery().equals(query)
        ));
    }

    @Test
    void whenSearch_withAuthenticatedUser_shouldLogEventWithCustomerId() {
        String query = "ibuprofeno";
        User userPrincipal = new User("test@user.com", "", Collections.emptyList());
        Customer mockCustomer = Customer.builder().id(42L).email("test@user.com").build();
        when(systemConfigRepository.getValueAsInt(anyString(), anyInt())).thenReturn(5);
        when(productRepository.findByNameContainingIgnoreCaseAndStockGreaterThan(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(customerRepositoryPort.findByEmail("test@user.com")).thenReturn(Optional.of(mockCustomer));
        SecurityContextHolder.setContext(securityContext);
        searchProductUseCase.searchProducts(query, request);
        verify(eventPublisher, times(1)).publishEvent(argThat(event ->
                event instanceof ProductSearchEvent &&
                        ((ProductSearchEvent) event).getCustomerId() == 42L
        ));
    }

    @Test
    void whenSearch_withQueryTooShort_shouldThrowSearchQueryException() {
        String query = "pa";
        Exception exception = assertThrows(SearchQueryException.class, () -> {
            searchProductUseCase.searchProducts(query, request);
        });
        assertEquals("La consulta de búsqueda debe tener al menos 3 caracteres.", exception.getMessage());
        verify(productRepository, never()).findByNameContainingIgnoreCaseAndStockGreaterThan(anyString(), anyInt());
    }
}