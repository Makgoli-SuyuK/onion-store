package com.example.onionstore.domain.refund.entity;

import com.example.onionstore.domain.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "refund_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_refund_item",
                columnNames = {"refund_id", "order_item_id"}
        )
)
public class RefundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private int quantity;

    public RefundItem(
            Refund refund,
            OrderItem orderItem,
            int quantity
    ) {
        this.refund = refund;
        this.orderItem = orderItem;
        this.quantity = quantity;
    }
}