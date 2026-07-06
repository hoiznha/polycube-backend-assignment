package com.polycube.payment.domain.discount;

import com.polycube.payment.domain.member.MemberGrade;
import com.polycube.payment.domain.order.Order;

public interface DiscountPolicy {

    boolean supports(MemberGrade memberGrade);

    DiscountResult discount(Order order);
}
