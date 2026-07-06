package com.polycube.payment.domain.discount;

import java.math.BigDecimal;

public record DiscountResult(
        String policyName,
        String target,
        BigDecimal discountRate,
        long discountAmount
) {

    public DiscountResult {
        if (policyName == null || policyName.isBlank()) {
            throw new IllegalArgumentException("할인 정책명은 필수입니다.");
        }
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("할인 대상은 필수입니다.");
        }
        if (discountRate == null) {
            throw new IllegalArgumentException("할인율은 필수입니다.");
        }
        if (discountRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인율은 0 이상이어야 합니다.");
        }
        if (discountAmount < 0) {
            throw new IllegalArgumentException("할인 금액은 0 이상이어야 합니다.");
        }
    }
}
