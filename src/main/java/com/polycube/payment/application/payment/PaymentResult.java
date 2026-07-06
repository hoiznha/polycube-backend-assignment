package com.polycube.payment.application.payment;

import com.polycube.payment.domain.payment.PaymentMethod;
import java.time.LocalDateTime;

public record PaymentResult(
        Long paymentId,
        Long orderId,
        long originalAmount,
        long discountAmount,
        long finalAmount,
        PaymentMethod paymentMethod,
        LocalDateTime paidAt
) {
}
