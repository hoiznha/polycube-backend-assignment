package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.order.Order;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class VvipRateDiscountPolicy implements DiscountPolicy {

    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    @Override
    public boolean supports(MemberGrade memberGrade) {
        return memberGrade == MemberGrade.VVIP;
    }

    @Override
    public DiscountResult discount(Order order) {
        validateOrder(order);
        long discountAmount = BigDecimal.valueOf(order.getOriginalAmount())
                .multiply(DISCOUNT_RATE)
                .setScale(0, RoundingMode.DOWN)
                .longValue();

        return new DiscountResult(
                "VvipRateDiscountPolicy",
                MemberGrade.VVIP.name(),
                DISCOUNT_RATE,
                discountAmount
        );
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
    }
}
