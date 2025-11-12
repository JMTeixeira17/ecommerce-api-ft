package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.CreateOrderRequest;
import com.farmatodo.ecommerce.application.dto.OrderResponse;
import com.farmatodo.ecommerce.application.mapper.OrderApiMapper;
import com.farmatodo.ecommerce.domain.exception.CartNotFoundException;
import com.farmatodo.ecommerce.domain.exception.InsufficientStockException;
import com.farmatodo.ecommerce.domain.exception.ProductNotFoundException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;
import com.farmatodo.ecommerce.domain.model.*;
import com.farmatodo.ecommerce.domain.port.in.CreateOrderUseCase;
import com.farmatodo.ecommerce.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final ShoppingCartRepositoryPort shoppingCartRepository;
    private final ProductRepositoryPort productRepository;
    private final CustomerRepositoryPort customerRepository;
    private final TokenizedCardRepositoryPort tokenizedCardRepository;
    private final OrderRepositoryPort orderRepository;
    private final OrderItemRepositoryPort orderItemRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final SystemConfigRepositoryPort systemConfigRepository;
    private final OrderApiMapper orderApiMapper;

    private static final String TAX_RATE_KEY = "tax.rate.percentage";
    private static final String MAX_ATTEMPTS_KEY = "payment.max.retry.attempts";

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(CreateOrderRequest request) {

        Customer customer = getAuthenticatedCustomer();
        Long customerId = customer.getId();

        ShoppingCart cart = shoppingCartRepository.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new CartNotFoundException("No se encontró un carrito activo para este usuario."));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartNotFoundException("Tu carrito de compras está vacío.");
        }

        TokenizedCard card = tokenizedCardRepository.findByUuidAndCustomerId(
                        UUID.fromString(request.getTokenizedCardUuid()),
                        customerId)
                .orElseThrow(() -> new TokenizationException("Tarjeta no encontrada o no pertenece a este usuario."));

        log.info("Iniciando creación de orden para cliente: {}", customerId);

        List<Product> productsToUpdate = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado. ID: " + item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Stock insuficiente para: " + product.getName());
            }
            product.setStock(product.getStock() - item.getQuantity());
            productsToUpdate.add(product);
            item.setProduct(product);
        }

        BigDecimal subtotal = cart.getTotalAmount();
        double taxRate = systemConfigRepository.getValueAsDouble(TAX_RATE_KEY, 16.0) / 100.0;
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);
        Order order = Order.builder()
                .customerId(customerId)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingPostalCode(request.getShippingPostalCode())
                .shippingCountry(request.getShippingCountry())
                .status(Order.OrderStatus.PENDING)
                .subtotal(subtotal)
                .tax(taxAmount)
                .shippingCost(shippingCost)
                .totalAmount(totalAmount)
                .build();

        Order savedOrder = orderRepository.save(order);
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            orderItems.add(OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(cartItem.getProductId())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .product(cartItem.getProduct())
                    .build());
        }
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(savedItems);

        productRepository.saveAll(productsToUpdate);

        int maxAttempts = systemConfigRepository.getValueAsInt(MAX_ATTEMPTS_KEY, 3);
        Payment payment = Payment.builder()
                .orderId(savedOrder.getId())
                .tokenizedCardId(card.getId())
                .amount(totalAmount)
                .currency("MXN")
                .status(Payment.PaymentStatus.PENDING)
                .attemptNumber(1)
                .maxAttempts(maxAttempts)
                .build();
        paymentRepository.save(payment);
        log.info("Registro de pago PENDING creado para la orden {}", savedOrder.getOrderNumber());

        cart.setStatus(ShoppingCart.CartStatus.CONVERTED);
        shoppingCartRepository.save(cart);

        savedOrder.setCustomer(customer);
        return orderApiMapper.toResponse(savedOrder);
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Se requiere autenticación de cliente para esta operación.");
        }
        String email = ((User) authentication.getPrincipal()).getUsername();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Cliente autenticado no encontrado en la base de datos."));
    }
}