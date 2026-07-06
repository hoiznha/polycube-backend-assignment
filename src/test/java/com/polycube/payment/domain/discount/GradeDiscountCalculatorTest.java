package com.polycube.payment.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.polycube.payment.domain.member.Member;
import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.order.Order;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GradeDiscountCalculatorTest {

    private GradeDiscountCalculator gradeDiscountCalculator;

    @BeforeEach
    void setUp() {
        gradeDiscountCalculator = new GradeDiscountCalculator(List.of(
                new NoDiscountPolicy(),
                new VipFixedDiscountPolicy(),
                new VvipRateDiscountPolicy()
        ));
    }

    @Test
    @DisplayName("NORMAL 회원은 할인이 적용되지 않는다")
    void normalMemberDiscount() {
        Order order = createOrder(MemberGrade.NORMAL, 10_000);

        DiscountResult result = gradeDiscountCalculator.calculate(order);

        assertThat(result.policyName()).isEqualTo("NoDiscountPolicy");
        assertThat(result.target()).isEqualTo("NORMAL");
        assertThat(result.discountRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.discountAmount()).isZero();
    }

    @Test
    @DisplayName("VIP 회원은 1,000원 고정 할인이 적용된다")
    void vipMemberDiscount() {
        Order order = createOrder(MemberGrade.VIP, 10_000);

        DiscountResult result = gradeDiscountCalculator.calculate(order);

        assertThat(result.policyName()).isEqualTo("VipFixedDiscountPolicy");
        assertThat(result.target()).isEqualTo("VIP");
        assertThat(result.discountRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.discountAmount()).isEqualTo(1_000);
    }

    @Test
    @DisplayName("VVIP 회원은 주문 금액의 10% 할인이 적용된다")
    void vvipMemberDiscount() {
        Order order = createOrder(MemberGrade.VVIP, 10_000);

        DiscountResult result = gradeDiscountCalculator.calculate(order);

        assertThat(result.policyName()).isEqualTo("VvipRateDiscountPolicy");
        assertThat(result.target()).isEqualTo("VVIP");
        assertThat(result.discountRate()).isEqualByComparingTo(new BigDecimal("0.10"));
        assertThat(result.discountAmount()).isEqualTo(1_000);
    }

    private Order createOrder(MemberGrade memberGrade, long originalAmount) {
        return new Order("테스트 상품", originalAmount, new Member(memberGrade));
    }
}
