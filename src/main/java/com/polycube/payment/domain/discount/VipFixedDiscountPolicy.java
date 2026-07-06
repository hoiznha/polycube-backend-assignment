package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.order.Order;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class VipFixedDiscountPolicy implements DiscountPolicy {

    private static final long DISCOUNT_AMOUNT = 1_000L;

    @Override
    public boolean supports(MemberGrade memberGrade) {
        return memberGrade == MemberGrade.VIP;
    }

    @Override
    public DiscountResult discount(Order order) {
        validateOrder(order);
        validateDiscountAmount(order);
        return new DiscountResult(
                "VipFixedDiscountPolicy",
                MemberGrade.VIP.name(),
                BigDecimal.ZERO,
                DISCOUNT_AMOUNT
        );
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
    }

    private void validateDiscountAmount(Order order) {
        if (DISCOUNT_AMOUNT > order.getOriginalAmount()) {
            throw new IllegalArgumentException("할인 금액은 주문 금액보다 클 수 없습니다.");
        }
    }
}
