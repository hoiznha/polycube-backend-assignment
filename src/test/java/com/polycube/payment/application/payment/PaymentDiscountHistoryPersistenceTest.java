package com.polycube.payment.application.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.polycube.payment.domain.member.Member;
import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.member.MemberRepository;
import com.polycube.payment.domain.payment.DiscountType;
import com.polycube.payment.domain.payment.PaymentDiscountHistory;
import com.polycube.payment.domain.payment.PaymentDiscountHistoryRepository;
import com.polycube.payment.domain.payment.PaymentMethod;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PaymentDiscountHistoryPersistenceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PaymentDiscountHistoryRepository paymentDiscountHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("결제 후 회원 등급이 변경되어도 결제 당시 할인 이력은 스냅샷으로 보존된다")
    void discountHistoryIsPreservedAfterMemberGradeChange() {
        Member member = memberRepository.save(new Member(MemberGrade.VVIP));
        PaymentResult paymentResult = paymentService.pay(new PaymentCommand(
                member.getId(),
                "테스트 상품",
                100_000,
                PaymentMethod.POINT
        ));

        member.changeGrade(MemberGrade.NORMAL);
        entityManager.flush();
        entityManager.clear();

        List<PaymentDiscountHistory> histories = paymentDiscountHistoryRepository
                .findByPaymentIdOrderByAppliedOrderAsc(paymentResult.paymentId());

        assertThat(histories).hasSize(2);

        PaymentDiscountHistory gradeHistory = histories.get(0);
        assertThat(gradeHistory.getDiscountType()).isEqualTo(DiscountType.GRADE);
        assertThat(gradeHistory.getPolicyName()).isEqualTo("VvipRateDiscountPolicy");
        assertThat(gradeHistory.getTarget()).isEqualTo("VVIP");
        assertThat(gradeHistory.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.10"));
        assertThat(gradeHistory.getDiscountAmount()).isEqualTo(10_000);
        assertThat(gradeHistory.getAppliedOrder()).isEqualTo(1);

        PaymentDiscountHistory paymentMethodHistory = histories.get(1);
        assertThat(paymentMethodHistory.getDiscountType()).isEqualTo(DiscountType.PAYMENT_METHOD);
        assertThat(paymentMethodHistory.getPolicyName()).isEqualTo("PointPaymentDiscountPolicy");
        assertThat(paymentMethodHistory.getTarget()).isEqualTo("POINT");
        assertThat(paymentMethodHistory.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.05"));
        assertThat(paymentMethodHistory.getDiscountAmount()).isEqualTo(4_500);
        assertThat(paymentMethodHistory.getAppliedOrder()).isEqualTo(2);
    }
}
