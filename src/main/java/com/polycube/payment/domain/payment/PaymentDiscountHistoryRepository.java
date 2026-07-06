package com.polycube.payment.domain.payment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDiscountHistoryRepository extends JpaRepository<PaymentDiscountHistory, Long> {

    List<PaymentDiscountHistory> findByPaymentIdOrderByAppliedOrderAsc(Long paymentId);
}
