package com.example.onionstore.domain.product;

import com.example.onionstore.domain.category.entity.Category;
import com.example.onionstore.domain.product.entity.Product;

public class ProductFixture {
    public static Product createProduct(int i, Category category) {
        return Product.create(
                category,
                "name " + i,
                "description " + i,
                1000 * i,
                5 * i
        );
    }
}
