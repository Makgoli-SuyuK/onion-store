package com.example.onionstore.domain.refund.entity;

import com.example.onionstore.domain.order.entity.OrderItem;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.user.entity.User;
import com.example.onionstore.global.entity.BaseTimeEntity;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refunds")
public class Refund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundInitiator initiator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RefundReasonType reasonType;

    @Column(nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "portone_cancellation_id", unique = true, length = 255)
    private String portoneCancellationId;

    @Column(name = "requested_cancellable_amount")
    private Long requestedCancellableAmount;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefundItem> items = new ArrayList<>();

    public void addItem(OrderItem orderItem, int quantity) {
        this.items.add(new RefundItem(this, orderItem, quantity));
    }

    public Refund(
            Payment payment,
            long amount,
            RefundStatus status,
            RefundInitiator initiator,
            RefundReasonType reasonType,
            String reason
    ) {
        this.payment = payment;
        this.amount = amount;
        this.status = status;
        this.initiator = initiator;
        this.reasonType = reasonType;
        this.reason = reason;
    }

    public void approve(User admin) {
        transitionTo(RefundStatus.REQUESTED);
        this.reviewedBy = admin;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User admin, String rejectionReason) {
        transitionTo(RefundStatus.REJECTED);
        this.reviewedBy = admin;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    public boolean complete() {
        if (status == RefundStatus.COMPLETED) {
            return false;
        }
        transitionTo(RefundStatus.COMPLETED);
        return true;
    }

    public boolean fail() {
        if (status == RefundStatus.FAILED) {
            return false;
        }
        transitionTo(RefundStatus.FAILED);
        return true;
    }

    public boolean assignCancellationId(String cancellationId) {
        if (cancellationId == null || cancellationId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CANCELLATION_ID);
        }

        if (this.portoneCancellationId == null) { this.portoneCancellationId = cancellationId;
            return true;
        }

        if (this.portoneCancellationId.equals(cancellationId)) {
            return false;
        }
        throw new BusinessException(ErrorCode.CANCELLATION_ALREADY_LINKED);
    }

    private void transitionTo(RefundStatus target) {
        if (!status.canTransitTo(target)) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }
        this.status = target;
    }
}
