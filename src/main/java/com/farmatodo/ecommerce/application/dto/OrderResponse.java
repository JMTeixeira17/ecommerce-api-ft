package com.farmatodo.ecommerce.application.dto;

import com.farmatodo.ecommerce.domain.model.Order;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder
public class OrderResponse {
    private UUID orderUuid;
    private String orderNumber;
    private Order.OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
    private List<OrderItemResponse> items;
}