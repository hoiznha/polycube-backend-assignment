package com.polycube.payment.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment_discount_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDiscountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private String policyName;

    @Column(nullable = false)
    private String target;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal discountRate;

    @Column(nullable = false)
    private long discountAmount;

    @Column(nullable = false)
    private int appliedOrder;

    public PaymentDiscountHistory(
            Payment payment,
            DiscountType discountType,
            String policyName,
            String target,
            BigDecimal discountRate,
            long discountAmount,
            int appliedOrder
    ) {
        validatePayment(payment);
        validateDiscountType(discountType);
        validateText(policyName, "할인 정책명은 필수입니다.");
        validateText(target, "할인 대상은 필수입니다.");
        validateDiscountRate(discountRate);
        validateDiscountAmount(discountAmount);
        validateAppliedOrder(appliedOrder);

        this.payment = payment;
        this.discountType = discountType;
        this.policyName = policyName;
        this.target = target;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.appliedOrder = appliedOrder;
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("결제 정보는 필수입니다.");
        }
    }

    private void validateDiscountType(DiscountType discountType) {
        if (discountType == null) {
            throw new IllegalArgumentException("할인 유형은 필수입니다.");
        }
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateDiscountRate(BigDecimal discountRate) {
        if (discountRate == null) {
            throw new IllegalArgumentException("할인율은 필수입니다.");
        }
        if (discountRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인율은 0 이상이어야 합니다.");
        }
    }

    private void validateDiscountAmount(long discountAmount) {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("할인 금액은 0 이상이어야 합니다.");
        }
    }

    private void validateAppliedOrder(int appliedOrder) {
        if (appliedOrder <= 0) {
            throw new IllegalArgumentException("할인 적용 순서는 1 이상이어야 합니다.");
        }
    }
}
