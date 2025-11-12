package com.farmatodo.ecommerce.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private Long customerId;
    private String customerEmail;
    private Long orderId;
    private Long paymentId;
    private String orderNumber;
}