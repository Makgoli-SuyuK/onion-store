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
    private int stock;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false)
    private boolean deleted;

    private Product(Category category, String name, String description, long price, int stock) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.likeCount = 0;
        this.status = stock == 0 ? ProductStatus.SOLD_OUT : ProductStatus.SELLING;
        this.deleted = false;
    }

    public static Product create(Category category, String name, String description, long price, int stock) {
        return new Product(category, name, description, price, stock);
    }

    public void decreaseStock(int quantity) {
        validateQuantity(quantity);

        if (status != ProductStatus.SELLING) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SELLING);
        }
        if (stock < quantity) {
            throw new BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        stock -= quantity;
        if (stock == 0) {
            status = ProductStatus.SOLD_OUT;
        }
    }

    public void restoreStock(int quantity) {
        validateQuantity(quantity);

        stock += quantity;
        if (status == ProductStatus.SOLD_OUT) {
            status = ProductStatus.SELLING;
        }
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void changePrice(long newPrice) {
        if (newPrice <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }

        this.price = newPrice;
    }

    public void changeStock(int newStock) {
        if (newStock < 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK);
        } else if (newStock == 0) {
            changeStatus(ProductStatus.SOLD_OUT);
        } else if (this.status != ProductStatus.SELLING) {
            changeStatus(ProductStatus.SELLING);
        }

        this.stock = newStock;
    }

    public void changeStatus(ProductStatus newStatus) {
        this.status = newStatus;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public void markAsDeleted() {
        this.deleted = true;
        this.status = ProductStatus.HIDDEN;
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public boolean isHidden() {
        return status == ProductStatus.HIDDEN;
    }
}
