package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.order.Order;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NoDiscountPolicy implements DiscountPolicy {

    @Override
    public boolean supports(MemberGrade memberGrade) {
        return memberGrade == MemberGrade.NORMAL;
    }

    @Override
    public DiscountResult discount(Order order) {
        validateOrder(order);
        return new DiscountResult(
                "NoDiscountPolicy",
                MemberGrade.NORMAL.name(),
                BigDecimal.ZERO,
                0
        );
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
    }
}
