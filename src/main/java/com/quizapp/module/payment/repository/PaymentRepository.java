package com.quizapp.module.payment.repository;

import com.quizapp.common.enums.PaymentStatus;
import com.quizapp.module.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    Page<Payment> findAll(Pageable pageable);
}
