package com.example.onionstore.domain.product.repository;

import com.example.onionstore.domain.product.dto.ProductSimpleResponse;
import com.example.onionstore.domain.product.repository.dto.ProductSearchConditions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.onionstore.domain.category.entity.QCategory.category;
import static com.example.onionstore.domain.product.entity.QProduct.product;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSimpleResponse> searchWithConditions(ProductSearchConditions searchConditions, Pageable pageable) {
        BooleanBuilder condition = allConditions(searchConditions);

        List<ProductSimpleResponse> content = queryFactory
                .select(Projections.constructor(ProductSimpleResponse.class,
                        product.id,
                        product.category.name.as("categoryName"),
                        product.name,
                        product.price,
                        product.likeCount
                        ))
                .from(product)
                .join(product.category, category)
                .where(condition)
                .orderBy(orderBy(searchConditions.sortBy(), searchConditions.sortOrder()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .toList();

        Long total = queryFactory.select(product.count())
                .from(product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0
        );
    }

    private BooleanBuilder allConditions(ProductSearchConditions searchConditions) {
        BooleanBuilder builder = new BooleanBuilder();

        return builder
                .and(searchWithCategory(searchConditions.category()))
                .and(searchLikeName(searchConditions.name()))
                .and(searchBetweenPrice(searchConditions.priceStart(), searchConditions.priceEnd()))
                .and(searchLikeCountGreaterThan(searchConditions.likeCount()))
                .and(isNotDeleted());
    }

    private BooleanExpression searchWithCategory(String categoryName) {
        return hasText(categoryName) ? product.category.name.eq(categoryName) : null;
    }

    private BooleanExpression searchLikeName(String name) {
        return hasText(name) ? product.name.like("%" + name + "%") : null;
    }

    private BooleanExpression searchBetweenPrice(Long low, Long high) {
        if (high == null && low == null) {
            return null;
        } else if (low != null && high == null) {
            return product.price.gt(low);
        } else if (low == null) {
            return product.price.lt(high);
        } else {
            return product.price.between(low, high);
        }
    }

    private BooleanExpression searchLikeCountGreaterThan(Integer count) {
        if (count == null) {
            return null;
        }

        return product.likeCount.goe(count);
    }

    private OrderSpecifier<?> orderBy(String sortBy, String sortOrder) {
        if (!hasText(sortBy)) {
            return null;
        }

        ComparableExpressionBase<?> targetPath = switch(sortBy) {
            case "name" -> product.name;
            case "category" -> category.name;
            case "price" -> product.price;
            case "likeCount" -> product.likeCount;
            default -> null;
        };

        if (targetPath == null) {
            return null;
        }

        return "asc".equals(sortOrder) ? targetPath.asc() : targetPath.desc();
    }

    private BooleanExpression isNotDeleted() {
        return product.deleted.isFalse();
    }
}
