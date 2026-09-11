package com.example.onionstore.domain.order.entity;

import com.example.onionstore.global.entity.BaseTimeEntity;
import com.example.onionstore.domain.user.entity.User;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", nullable = false)
    private long totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public Order(User user, long totalPrice) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
        this.orderNumber = makeOrderNumber();
    }

    public boolean markAsPaid() {
        return changeStatus(OrderStatus.PAID);
    }

    public boolean cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            return false;
        }
        changeStatus(OrderStatus.CANCELLED);
        this.cancelledAt = LocalDateTime.now();
        return true;
    }

    public boolean adminChangeOrderStatus(OrderStatus status) {
        return changeStatus(status);
    }

    private boolean changeStatus(OrderStatus target) {
        if (status == target) {
            return false;
        }
        if (!status.canTransitTo(target)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        status = target;
        return true;
    }

    private String makeOrderNumber() {
        String uuid = UUID.randomUUID().toString();
        String reUuid = uuid.replace("-", "");
        return "ORD-" + reUuid.substring(0, 16);
    }
}
