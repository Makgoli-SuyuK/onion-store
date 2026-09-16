package com.example.onionstore.domain.order.entity;

import com.example.onionstore.global.entity.BaseCreatedTimeEntity;
import com.example.onionstore.domain.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private long productPrice;

    @Column(nullable = false)
    private int quantity;

    // 결제가 확정될 때 주문에 사용한 장바구니 항목만 비우기 위한 원본 항목 ID다.
    @Column(name = "source_cart_item_id")
    private Long sourceCartItemId;

    public OrderItem(Order order, Product product, String productName, long productPrice, int quantity) {
        this(order, product, productName, productPrice, quantity, null);
    }

    public OrderItem(Order order, Product product, String productName, long productPrice,
                     int quantity, Long sourceCartItemId) {
        this.order = order;
        this.product = product;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.sourceCartItemId = sourceCartItemId;
    }

}
