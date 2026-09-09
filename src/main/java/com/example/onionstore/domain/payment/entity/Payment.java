package com.example.onionstore.domain.payment.entity;

import com.example.onionstore.global.entity.BaseTimeEntity;
import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
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
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "portone_payment_id", unique = true, length = 255)
    private String portonePaymentId;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment(Order order, long amount) {
        this.order = order;
        this.amount = amount;
        this.portonePaymentId = "pay_" + UUID.randomUUID();
        this.status = PaymentStatus.READY;
    }

    public boolean markAsSuccess() {
        boolean changed = changedStatus(PaymentStatus.SUCCESS);
        if(changed) {
            this.paidAt = LocalDateTime.now();
        }
        return changed;
    }

    public boolean markAsFailed() {
        return changedStatus(PaymentStatus.FAILED);
    }

    public boolean markAsCancelled() {
        return changedStatus(PaymentStatus.CANCELLED);
    }


    private boolean changedStatus(PaymentStatus target) {
        if (this.status == target) {
            return false;
        }
        if (!this.status.canTransitTo(target)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        this.status = target;
        return true;
    }
}
