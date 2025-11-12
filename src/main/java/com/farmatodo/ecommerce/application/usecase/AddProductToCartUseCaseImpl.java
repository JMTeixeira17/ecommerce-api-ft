package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.AddItemRequest;
import com.farmatodo.ecommerce.application.dto.CartResponse;
import com.farmatodo.ecommerce.application.mapper.CartApiMapper;
import com.farmatodo.ecommerce.domain.exception.InsufficientStockException;
import com.farmatodo.ecommerce.domain.exception.ProductNotFoundException;
import com.farmatodo.ecommerce.domain.model.CartItem;
import com.farmatodo.ecommerce.domain.model.Product;
import com.farmatodo.ecommerce.domain.model.ShoppingCart;
import com.farmatodo.ecommerce.domain.port.in.AddProductToCartUseCase;
import com.farmatodo.ecommerce.domain.port.out.CartItemRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.ProductRepositoryPort;
import com.farmatodo.ecommerce.domain.port.out.ShoppingCartRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddProductToCartUseCaseImpl implements AddProductToCartUseCase {

    private final ShoppingCartRepositoryPort shoppingCartRepository;
    private final CartItemRepositoryPort cartItemRepository;
    private final ProductRepositoryPort productRepository;
    private final CustomerRepositoryPort customerRepository;
    private final CartApiMapper cartApiMapper;

    public static final String SESSION_ID_HEADER = "X-Session-ID";

    @Override
    @Transactional
    public CartResponse addProductToCart(AddItemRequest request, HttpServletRequest servletRequest) {

        ShoppingCart cart = getOrCreateCart(servletRequest);

        Product product = productRepository.findByIdAndIsActiveTrue(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado o no está activo. ID: " + request.getProductId()));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst();

        CartItem itemToSave;
        if (existingItemOpt.isPresent()) {
            itemToSave = existingItemOpt.get();
            int newQuantity = itemToSave.getQuantity() + request.getQuantity();

            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException(String.format(
                        "Stock insuficiente para '%s'. Solicitado: %d, Ya en carrito: %d, Stock: %d",
                        product.getName(), request.getQuantity(), itemToSave.getQuantity(), product.getStock()
                ));
            }
            itemToSave.setQuantity(newQuantity);

        } else {
            if (product.getStock() < request.getQuantity()) {
                throw new InsufficientStockException(String.format(
                        "Stock insuficiente para '%s'. Solicitado: %d, Stock: %d",
                        product.getName(), request.getQuantity(), product.getStock()
                ));
            }
            itemToSave = CartItem.builder()
                    .cartId(cart.getId())
                    .productId(product.getId())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .product(product)
                    .build();
        }

        CartItem savedItem = cartItemRepository.save(itemToSave);
        cart.getItems().removeIf(item -> item.getProductId().equals(savedItem.getProductId()));
        savedItem.setProduct(product);
        cart.getItems().add(savedItem);
        ShoppingCart updatedCart = updateCartTotals(cart);
        CartResponse response = cartApiMapper.toResponse(updatedCart);
        if (updatedCart.getSessionId() != null) {
            response.setSessionId(updatedCart.getSessionId());
        }
        return response;
    }

    private ShoppingCart getOrCreateCart(HttpServletRequest request) {
        Long customerId = getAuthenticatedCustomerId();
        String sessionId = request.getHeader(SESSION_ID_HEADER);

        if (customerId != null) {
            Optional<ShoppingCart> cartOpt = shoppingCartRepository.findActiveByCustomerId(customerId);
            if (cartOpt.isPresent()) {
                return cartOpt.get();
            }
            return createNewCart(customerId, null, request);

        } else {
            if (sessionId != null) {
                Optional<ShoppingCart> cartOpt = shoppingCartRepository.findActiveBySessionId(sessionId);
                if (cartOpt.isPresent()) {
                    return cartOpt.get();
                }
            }
            String newSessionId = UUID.randomUUID().toString();
            return createNewCart(null, newSessionId, request);
        }
    }

    private ShoppingCart createNewCart(Long customerId, String sessionId, HttpServletRequest request) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(72);
        ShoppingCart newCart = ShoppingCart.builder()
                .customerId(customerId)
                .sessionId(sessionId)
                .status(ShoppingCart.CartStatus.ACTIVE)
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .expiresAt(expiresAt)
                .build();
        return shoppingCartRepository.save(newCart);
    }

    private ShoppingCart updateCartTotals(ShoppingCart cart) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;
        if (cart.getItems() == null) {
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setTotalItems(0);
            return shoppingCartRepository.save(cart);
        }

        for (CartItem item : cart.getItems()) {
            BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(itemSubtotal);

            totalAmount = totalAmount.add(itemSubtotal);
            totalItems += item.getQuantity();
        }

        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
        return shoppingCartRepository.save(cart);
    }

    private Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            String email = ((User) principal).getUsername();
            return customerRepository.findByEmail(email)
                    .map(customer -> customer.getId())
                    .orElse(null);
        }
        return null;
    }
}