package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.CreateOrderRequest;
import com.farmatodo.ecommerce.application.dto.OrderResponse;
import com.farmatodo.ecommerce.application.mapper.OrderApiMapper;
import com.farmatodo.ecommerce.domain.exception.CartNotFoundException;
import com.farmatodo.ecommerce.domain.exception.InsufficientStockException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;
import com.farmatodo.ecommerce.domain.model.*;
import com.farmatodo.ecommerce.domain.port.out.*;
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
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseImplTest {

    @Mock private ShoppingCartRepositoryPort shoppingCartRepository;
    @Mock private ProductRepositoryPort productRepository;
    @Mock private CustomerRepositoryPort customerRepository;
    @Mock private TokenizedCardRepositoryPort tokenizedCardRepository;
    @Mock private OrderRepositoryPort orderRepository;
    @Mock private OrderItemRepositoryPort orderItemRepository;
    @Mock private PaymentRepositoryPort paymentRepository;
    @Mock private SystemConfigRepositoryPort systemConfigRepository;
    @Mock private OrderApiMapper orderApiMapper;

    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private CreateOrderUseCaseImpl createOrderUseCase;

    private Customer customer;
    private ShoppingCart cart;
    private Product product;
    private TokenizedCard card;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).email("test@user.com").build();
        product = Product.builder().id(1L).name("Test Product").stock(10).price(new BigDecimal("100")).build();
        card = TokenizedCard.builder().id(1L).customerId(1L).build();

        CartItem cartItem = CartItem.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("100"))
                .product(product)
                .build();

        cart = ShoppingCart.builder()
                .id(1L)
                .customerId(1L)
                .items(new java.util.ArrayList<>(Collections.singletonList(cartItem)))
                .totalAmount(new BigDecimal("200"))
                .build();

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setTokenizedCardUuid(UUID.randomUUID().toString());
        createOrderRequest.setShippingAddressLine1("123 Main St");
        createOrderRequest.setShippingCity("City");
        createOrderRequest.setShippingState("State");
        createOrderRequest.setShippingPostalCode("12345");
        createOrderRequest.setShippingCountry("MX");
    }

    private void mockSecurityContext() {
        User userPrincipal = new User(customer.getEmail(), "", Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        SecurityContextHolder.setContext(securityContext);
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
    }

    @Test
    void whenCreateOrder_withValidCart_shouldSucceedAndConvertCart() {
        mockSecurityContext();

        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(tokenizedCardRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(card));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(16.0);
        when(systemConfigRepository.getValueAsInt(anyString(), anyInt())).thenReturn(3);
        Order savedOrder = Order.builder().id(100L).orderNumber("ORD-TEST").build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(productRepository).saveAll(anyList());
        when(orderApiMapper.toResponse(any(Order.class))).thenReturn(OrderResponse.builder().orderNumber("ORD-TEST").build());
        createOrderUseCase.createOrderFromCart(createOrderRequest);
        verify(shoppingCartRepository, times(1)).save(argThat(
                savedCart -> savedCart.getStatus() == ShoppingCart.CartStatus.CONVERTED
        ));
        verify(productRepository, times(1)).saveAll(argThat(list ->
                list.get(0).getStock() == 8
        ));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void whenCreateOrder_withNoActiveCart_shouldThrowCartNotFoundException() {
        mockSecurityContext();
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(CartNotFoundException.class, () -> {
            createOrderUseCase.createOrderFromCart(createOrderRequest);
        });
        assertEquals("No se encontró un carrito activo para este usuario.", exception.getMessage());
    }

    @Test
    void whenCreateOrder_withValidCart_shouldSucceed() {
        mockSecurityContext();
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(tokenizedCardRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(card));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(16.0);
        when(systemConfigRepository.getValueAsInt(anyString(), anyInt())).thenReturn(3);
        Order savedOrder = Order.builder().id(100L).build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(orderApiMapper.toResponse(any(Order.class))).thenReturn(OrderResponse.builder().orderNumber("ORD-123").build());
        OrderResponse response = createOrderUseCase.createOrderFromCart(createOrderRequest);
        assertNotNull(response);
        assertEquals("ORD-123", response.getOrderNumber());
        verify(productRepository, times(1)).saveAll(argThat(list ->
                list.get(0).getStock() == 8
        ));
        verify(shoppingCartRepository, times(1)).save(argThat(
                savedCart -> savedCart.getStatus() == ShoppingCart.CartStatus.CONVERTED
        ));
        verify(paymentRepository, times(1)).save(argThat(
                payment -> payment.getStatus() == Payment.PaymentStatus.PENDING
        ));
    }

    @Test
    void whenCreateOrder_withEmptyCart_shouldThrowCartNotFoundException() {
        mockSecurityContext();
        cart.setItems(Collections.emptyList());
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        Exception exception = assertThrows(CartNotFoundException.class, () -> {
            createOrderUseCase.createOrderFromCart(createOrderRequest);
        });
        assertEquals("Tu carrito de compras está vacío.", exception.getMessage());
    }

    @Test
    void whenCreateOrder_withInsufficientStock_shouldThrowInsufficientStockException() {
        mockSecurityContext();
        product.setStock(1);
        cart.getItems().get(0).setQuantity(2);
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(tokenizedCardRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.of(card));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        Exception exception = assertThrows(InsufficientStockException.class, () -> {
            createOrderUseCase.createOrderFromCart(createOrderRequest);
        });
        assertTrue(exception.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void whenCreateOrder_withInvalidCardUuid_shouldThrowTokenizationException() {
        mockSecurityContext();
        when(shoppingCartRepository.findActiveByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(tokenizedCardRepository.findByUuidAndCustomerId(any(UUID.class), anyLong())).thenReturn(Optional.empty());
        Exception exception = assertThrows(TokenizationException.class, () -> {
            createOrderUseCase.createOrderFromCart(createOrderRequest);
        });
        assertTrue(exception.getMessage().contains("Tarjeta no encontrada o no pertenece a este usuario."));
    }
}