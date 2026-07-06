package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.payment.PaymentMethod;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMethodDiscountCalculator {

    private final List<PaymentMethodDiscountPolicy> paymentMethodDiscountPolicies;

    public Optional<DiscountResult> calculate(long amount, PaymentMethod paymentMethod) {
        validateAmount(amount);
        validatePaymentMethod(paymentMethod);

        return paymentMethodDiscountPolicies.stream()
                .filter(policy -> policy.supports(paymentMethod))
                .findFirst()
                .map(policy -> policy.discount(amount));
    }

    private void validateAmount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("할인 대상 금액은 0 이상이어야 합니다.");
        }
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 수단은 필수입니다.");
        }
    }
}
