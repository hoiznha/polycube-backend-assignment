package com.polycube.payment.application.payment;

import com.polycube.payment.domain.discount.DiscountResult;
import com.polycube.payment.domain.discount.GradeDiscountCalculator;
import com.polycube.payment.domain.discount.PaymentMethodDiscountCalculator;
import com.polycube.payment.domain.member.Member;
import com.polycube.payment.domain.member.MemberRepository;
import com.polycube.payment.domain.order.Order;
import com.polycube.payment.domain.order.OrderRepository;
import com.polycube.payment.domain.payment.DiscountType;
import com.polycube.payment.domain.payment.Payment;
import com.polycube.payment.domain.payment.PaymentDiscountHistory;
import com.polycube.payment.domain.payment.PaymentDiscountHistoryRepository;
import com.polycube.payment.domain.payment.PaymentRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentDiscountHistoryRepository paymentDiscountHistoryRepository;
    private final GradeDiscountCalculator gradeDiscountCalculator;
    private final PaymentMethodDiscountCalculator paymentMethodDiscountCalculator;

    @Transactional
    public PaymentResult pay(PaymentCommand command) {
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Order order = orderRepository.save(new Order(
                command.productName(),
                command.originalAmount(),
                member
        ));

        DiscountResult gradeDiscountResult = gradeDiscountCalculator.calculate(order);
        long gradeDiscountedAmount = calculateFinalAmount(order.getOriginalAmount(), gradeDiscountResult.discountAmount());
        Optional<DiscountResult> paymentMethodDiscountResult = paymentMethodDiscountCalculator.calculate(
                gradeDiscountedAmount,
                command.paymentMethod()
        );
        long paymentMethodDiscountAmount = paymentMethodDiscountResult
                .map(DiscountResult::discountAmount)
                .orElse(0L);
        long finalAmount = calculateFinalAmount(gradeDiscountedAmount, paymentMethodDiscountAmount);

        Payment payment = paymentRepository.save(new Payment(
                order,
                order.getOriginalAmount(),
                finalAmount,
                command.paymentMethod(),
                LocalDateTime.now()
        ));
        saveGradeDiscountHistory(payment, gradeDiscountResult);
        paymentMethodDiscountResult.ifPresent(discountResult ->
                savePaymentMethodDiscountHistory(payment, discountResult)
        );

        return new PaymentResult(
                payment.getId(),
                order.getId(),
                order.getOriginalAmount(),
                gradeDiscountResult.discountAmount(),
                paymentMethodDiscountAmount,
                gradeDiscountResult.discountAmount() + paymentMethodDiscountAmount,
                payment.getFinalAmount(),
                payment.getPaymentMethod(),
                payment.getPaidAt()
        );
    }

    private void saveGradeDiscountHistory(Payment payment, DiscountResult discountResult) {
        paymentDiscountHistoryRepository.save(new PaymentDiscountHistory(
                payment,
                DiscountType.GRADE,
                discountResult.policyName(),
                discountResult.target(),
                discountResult.discountRate(),
                discountResult.discountAmount(),
                1
        ));
    }

    private void savePaymentMethodDiscountHistory(Payment payment, DiscountResult discountResult) {
        paymentDiscountHistoryRepository.save(new PaymentDiscountHistory(
                payment,
                DiscountType.PAYMENT_METHOD,
                discountResult.policyName(),
                discountResult.target(),
                discountResult.discountRate(),
                discountResult.discountAmount(),
                2
        ));
    }

    private long calculateFinalAmount(long originalAmount, long discountAmount) {
        if (discountAmount > originalAmount) {
            throw new IllegalArgumentException("할인 금액은 주문 금액보다 클 수 없습니다.");
        }
        return originalAmount - discountAmount;
    }
}
