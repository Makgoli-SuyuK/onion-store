package com.example.onionstore.domain.payment.repository;

import com.example.onionstore.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
