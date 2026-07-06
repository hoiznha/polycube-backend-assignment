package com.polycube.payment.application.payment;

import com.polycube.payment.domain.discount.DiscountResult;
import com.polycube.payment.domain.discount.GradeDiscountCalculator;
import com.polycube.payment.domain.member.Member;
import com.polycube.payment.domain.member.MemberRepository;
import com.polycube.payment.domain.order.Order;
import com.polycube.payment.domain.order.OrderRepository;
import com.polycube.payment.domain.payment.Payment;
import com.polycube.payment.domain.payment.PaymentRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final GradeDiscountCalculator gradeDiscountCalculator;

    @Transactional
    public PaymentResult pay(PaymentCommand command) {
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Order order = orderRepository.save(new Order(
                command.productName(),
                command.originalAmount(),
                member
        ));

        DiscountResult discountResult = gradeDiscountCalculator.calculate(order);
        long finalAmount = calculateFinalAmount(order.getOriginalAmount(), discountResult.discountAmount());

        Payment payment = paymentRepository.save(new Payment(
                order,
                finalAmount,
                command.paymentMethod(),
                LocalDateTime.now()
        ));

        return new PaymentResult(
                payment.getId(),
                order.getId(),
                order.getOriginalAmount(),
                discountResult.discountAmount(),
                payment.getFinalAmount(),
                payment.getPaymentMethod(),
                payment.getPaidAt()
        );
    }

    private long calculateFinalAmount(long originalAmount, long discountAmount) {
        if (discountAmount > originalAmount) {
            throw new IllegalArgumentException("할인 금액은 주문 금액보다 클 수 없습니다.");
        }
        return originalAmount - discountAmount;
    }
}
