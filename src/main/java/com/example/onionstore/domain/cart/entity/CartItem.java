package com.example.onionstore.domain.cart.entity;

import com.example.onionstore.global.entity.BaseTimeEntity;
import com.example.onionstore.domain.product.entity.Product;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_item_cart_product", columnNames = {"cart_id", "product_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    public CartItem(Cart cart, Product product, int quantity) {
        validateQuantity(quantity);
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) { // 같은 상품을 다시 담으면 새 행 대신 기존 수량을 증가시킨다.
        validateQuantity(quantity);
        this.quantity += quantity;
    }

    public void changeQuantity(int quantity) { // 장바구니 항목의 수량을 요청한 값으로 바꾼다.
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    private void validateQuantity(int quantity) { // 생성과 수량 변경 모두에서 1개 이상 규칙을 보장한다.
        if (quantity < 1) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEM_QUANTITY);
        }
    }
}
