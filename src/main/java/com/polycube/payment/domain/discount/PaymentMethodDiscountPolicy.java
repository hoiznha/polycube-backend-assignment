package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.payment.PaymentMethod;

public interface PaymentMethodDiscountPolicy {

    boolean supports(PaymentMethod paymentMethod);

    DiscountResult discount(long amount);
}
