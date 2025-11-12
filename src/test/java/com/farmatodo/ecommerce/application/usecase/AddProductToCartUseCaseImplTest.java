package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.AddItemRequest;
import com.farmatodo.ecommerce.application.dto.CartResponse;
import com.farmatodo.ecommerce.application.mapper.CartApiMapper;
import com.farmatodo.ecommerce.domain.exception.InsufficientStockException;
import com.farmatodo.ecommerce.domain.exception.ProductNotFoundException;
import com.farmatodo.ecommerce.domain.model.CartItem;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import com.farmatodo.ecommerce.domain.port.out.CartItemRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.ProductRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.ShoppingCartRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddProductToCartUseCaseImplTest {

    @Mock private ShoppingCartRepositoryPort shoppingCartRepository;
    @Mock private CartItemRepositoryPort cartItemRepository;
    @Mock private ProductRepositoryPort productRepository;
    @Mock private CustomerRepositoryPort customerRepository;
    @Mock private CartApiMapper cartApiMapper;
    @Mock private HttpServletRequest request;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private AddProductToCartUseCaseImpl addProductToCartUseCase;

    private Product product;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Paracetamol")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        cart = ShoppingCart.builder()
                .id(1L)
                .customerId(1L)
                .status(ShoppingCart.CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
    }

    private void mockAuthenticatedUser(Long customerId, String email) {
        User userPrincipal = new User(email, "", Collections.emptyList());
        Customer mockCustomer = Customer.builder().id(customerId).email(email).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(mockCustomer));
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAnonymousUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void whenAddProduct_asNewAuthenticatedUser_shouldCreateCartAndAddItem() {
        mockAuthenticatedUser(1L, "test@user.com");
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.empty());
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(cart);
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartApiMapper.toResponse(any(ShoppingCart.class))).thenReturn(CartResponse.builder().totalItems(2).build());

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setProductId(1L);
        addItemRequest.setQuantity(2);
        CartResponse response = addProductToCartUseCase.addProductToCart(addItemRequest, request);
        assertEquals(2, response.getTotalItems());
        verify(shoppingCartRepository, times(2)).save(any(ShoppingCart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void whenAddProduct_toExistingItem_shouldUpdateQuantity() {
        mockAuthenticatedUser(1L, "test@user.com");
        CartItem existingItem = CartItem.builder()
                .id(10L)
                .cartId(1L)
                .productId(1L)
                .quantity(3)
                .unitPrice(new BigDecimal("100.00"))
                .product(product)
                .build();
        cart.getItems().add(existingItem);

        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(shoppingCartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(cartApiMapper.toResponse(any(ShoppingCart.class)))
                .thenReturn(CartResponse.builder().totalItems(5).build());

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setProductId(1L);
        addItemRequest.setQuantity(2);
        CartResponse response = addProductToCartUseCase.addProductToCart(addItemRequest, request);
        assertEquals(5, response.getTotalItems());
        verify(cartItemRepository, times(1)).save(argThat(
                item -> item.getQuantity() == 5
        ));
    }

    @Test
    void whenAddProduct_withInsufficientStock_shouldThrowInsufficientStockException() {
        mockAuthenticatedUser(1L, "test@user.com");
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setProductId(1L);
        addItemRequest.setQuantity(11);

        Exception exception = assertThrows(InsufficientStockException.class, () -> {
            addProductToCartUseCase.addProductToCart(addItemRequest, request);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void whenAddProduct_asAnonymousUser_shouldCreateSessionCart() {
        mockAnonymousUser();
        when(request.getHeader("X-Session-ID")).thenReturn(null);

        ShoppingCart sessionCart = ShoppingCart.builder()
                .id(2L)
                .sessionId("new-session-id")
                .status(ShoppingCart.CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(sessionCart);
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartApiMapper.toResponse(any(ShoppingCart.class))).thenReturn(CartResponse.builder().sessionId("new-session-id").build());

        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setProductId(1L);
        addItemRequest.setQuantity(1);

        CartResponse response = addProductToCartUseCase.addProductToCart(addItemRequest, request);

        assertNotNull(response.getSessionId());
        assertEquals("new-session-id", response.getSessionId());
        verify(shoppingCartRepository, times(2)).save(any(ShoppingCart.class));
    }

    @Test
    void whenAddProduct_toExistingItem_withInsufficientStock_shouldThrowException() {
        mockAuthenticatedUser(1L, "test@user.com");
        CartItem existingItem = CartItem.builder()
                .id(10L)
                .cartId(1L)
                .productId(1L)
                .quantity(7)
                .unitPrice(new BigDecimal("100.00"))
                .product(product)
                .build();
        cart.getItems().add(existingItem);

        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(product));
        AddItemRequest addItemRequest = new AddItemRequest();
        addItemRequest.setProductId(1L);
        addItemRequest.setQuantity(5);
        Exception exception = assertThrows(InsufficientStockException.class, () -> {
            addProductToCartUseCase.addProductToCart(addItemRequest, request);
        });
        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        assertTrue(exception.getMessage().contains("Paracetamol"));
        assertTrue(exception.getMessage().contains("Solicitado: 5"));
        assertTrue(exception.getMessage().contains("Ya en carrito: 7"));
        assertTrue(exception.getMessage().contains("Stock: 10"));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }
}