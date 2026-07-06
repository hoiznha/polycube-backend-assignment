package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.payment.PaymentMethod;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PointPaymentDiscountPolicy implements PaymentMethodDiscountPolicy {

    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.POINT;
    }

    @Override
    public DiscountResult discount(long amount) {
        validateAmount(amount);
        long discountAmount = BigDecimal.valueOf(amount)
                .multiply(DISCOUNT_RATE)
                .setScale(0, RoundingMode.DOWN)
                .longValue();

        return new DiscountResult(
                "PointPaymentDiscountPolicy",
                PaymentMethod.POINT.name(),
                DISCOUNT_RATE,
                discountAmount
        );
    }

    private void validateAmount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("할인 대상 금액은 0 이상이어야 합니다.");
        }
    }
}
