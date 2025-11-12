package com.farmatodo.ecommerce.domain.port.in;

import com.farmatodo.ecommerce.application.dto.PaymentResponse;


public interface ProcessPaymentUseCase {

    PaymentResponse processOrderPayment(String orderUuid);
}