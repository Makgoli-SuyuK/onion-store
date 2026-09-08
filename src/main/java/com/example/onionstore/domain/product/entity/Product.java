package com.example.onionstore.domain.product.entity;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.global.entity.BaseTimeEntity;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private long stock;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false)
    private boolean deleted;

    private Product(Category category, String name, String description, long price, long stock) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.likeCount = 0;
        this.status = stock == 0 ? ProductStatus.SOLD_OUT : ProductStatus.SELLING;
        this.deleted = false;
    }

    public static Product create(Category category, String name, String description, long price, long stock) {
        return new Product(category, name, description, price, stock);
    }
}
