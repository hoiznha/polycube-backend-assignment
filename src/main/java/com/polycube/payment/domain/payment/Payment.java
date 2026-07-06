package com.polycube.payment.domain.payment;

import com.polycube.payment.domain.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private long originalAmount;

    @Column(nullable = false)
    private long finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    public Payment(Order order, long originalAmount, long finalAmount, PaymentMethod paymentMethod, LocalDateTime paidAt) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
        if (originalAmount <= 0) {
            throw new IllegalArgumentException("주문 원가는 0보다 커야 합니다.");
        }
        if (finalAmount < 0) {
            throw new IllegalArgumentException("최종 결제 금액은 0 이상이어야 합니다.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 수단은 필수입니다.");
        }
        if (paidAt == null) {
            throw new IllegalArgumentException("결제 일시는 필수입니다.");
        }
        this.order = order;
        this.originalAmount = originalAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
    }
}
