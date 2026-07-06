package com.polycube.payment.application.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.polycube.payment.domain.member.Member;
import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.member.MemberRepository;
import com.polycube.payment.domain.payment.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PaymentServiceDuplicateDiscountTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("VVIP 회원이 포인트로 결제하면 등급 할인 후 남은 금액에 결제 수단 할인이 적용된다")
    void vvipPointPaymentAppliesPaymentMethodDiscountAfterGradeDiscount() {
        Member member = memberRepository.save(new Member(MemberGrade.VVIP));

        PaymentResult result = paymentService.pay(new PaymentCommand(
                member.getId(),
                "테스트 상품",
                100_000,
                PaymentMethod.POINT
        ));

        assertThat(result.originalAmount()).isEqualTo(100_000);
        assertThat(result.gradeDiscountAmount()).isEqualTo(10_000);
        assertThat(result.paymentMethodDiscountAmount()).isEqualTo(4_500);
        assertThat(result.totalDiscountAmount()).isEqualTo(14_500);
        assertThat(result.finalAmount()).isEqualTo(85_500);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.POINT);
    }

    @Test
    @DisplayName("신용카드 결제는 결제 수단 추가 할인이 적용되지 않는다")
    void creditCardPaymentDoesNotApplyPaymentMethodDiscount() {
        Member member = memberRepository.save(new Member(MemberGrade.VVIP));

        PaymentResult result = paymentService.pay(new PaymentCommand(
                member.getId(),
                "테스트 상품",
                100_000,
                PaymentMethod.CREDIT_CARD
        ));

        assertThat(result.originalAmount()).isEqualTo(100_000);
        assertThat(result.gradeDiscountAmount()).isEqualTo(10_000);
        assertThat(result.paymentMethodDiscountAmount()).isZero();
        assertThat(result.totalDiscountAmount()).isEqualTo(10_000);
        assertThat(result.finalAmount()).isEqualTo(90_000);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }
}
