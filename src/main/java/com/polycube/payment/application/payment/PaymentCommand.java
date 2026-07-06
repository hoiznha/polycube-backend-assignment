package com.polycube.payment.application.payment;

import com.polycube.payment.domain.payment.PaymentMethod;

public record PaymentCommand(
        Long memberId,
        String productName,
        long originalAmount,
        PaymentMethod paymentMethod
) {

    public PaymentCommand {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (originalAmount <= 0) {
            throw new IllegalArgumentException("주문 금액은 0보다 커야 합니다.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 수단은 필수입니다.");
        }
    }
}
