package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.order.Order;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeDiscountCalculator {

    private final List<DiscountPolicy> discountPolicies;

    public DiscountResult calculate(Order order) {
        validateOrder(order);
        MemberGrade memberGrade = order.getMember().getGrade();

        DiscountPolicy discountPolicy = discountPolicies.stream()
                .filter(policy -> policy.supports(memberGrade))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("적용 가능한 할인 정책이 없습니다."));

        DiscountResult discountResult = discountPolicy.discount(order);
        validateDiscountAmount(order, discountResult);
        return discountResult;
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
        if (order.getMember() == null) {
            throw new IllegalArgumentException("회원 정보는 필수입니다.");
        }
        if (order.getMember().getGrade() == null) {
            throw new IllegalArgumentException("회원 등급은 필수입니다.");
        }
    }

    private void validateDiscountAmount(Order order, DiscountResult discountResult) {
        if (discountResult.discountAmount() > order.getOriginalAmount()) {
            throw new IllegalArgumentException("할인 금액은 주문 금액보다 클 수 없습니다.");
        }
    }
}
