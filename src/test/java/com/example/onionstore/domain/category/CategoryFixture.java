package com.example.onionstore.domain.category;

import com.example.onionstore.domain.category.entity.Category;

public class CategoryFixture {
    public static Category createCategory(int i, boolean deleted) {
        Category newCategory = new Category("category " + i);

        if (deleted) {
            newCategory.markAsDeleted();
        }

        return newCategory;
    }
}
