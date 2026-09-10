package com.example.onionstore.domain.order.repository;

import com.example.onionstore.domain.order.dto.OrderSearchRequest;
import com.example.onionstore.domain.order.entity.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static com.example.onionstore.domain.order.entity.QOrder.order;
import static com.example.onionstore.domain.order.entity.QOrderItem.orderItem;

@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findAllByUser_IdWithKeyword(Long userId, Pageable pageable, OrderSearchRequest request) {

        // 실제로 현재 페이지에 보여줄 주문 데이터를 조회
        List<Order> orders = queryFactory
                .selectFrom(order)
                .join(orderItem)
                .on(orderItem.order.id.eq(order.id))
                .where(order.user.id.eq(userId),
                        keywordContain(request.getKeyword()),
                        startDateGoe(request.getStartDate()),
                        endDateLt(request.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 주문 개수를 조회
        Long total = queryFactory
                .select(order.id.countDistinct())
                .from(order)
                .join(orderItem)
                .on(orderItem.order.id.eq(order.id))
                .where(order.user.id.eq(userId),
                        keywordContain(request.getKeyword()),
                        startDateGoe(request.getStartDate()),
                        endDateLt(request.getEndDate())
                )
                .fetchOne();

        // 조회한 주문 목록 + 페이징 정보 + 전체 데이터 개수를 Page<Order> 객체로 반환
        return new PageImpl<>(orders, pageable, total);
    }

    private BooleanExpression keywordContain(String keyword) {
        return keyword == null || keyword.isBlank()
                ? null
                : orderItem.product.name.contains(keyword);
    }
    private BooleanExpression startDateGoe(LocalDate startDate) {
        return startDate != null
                ? order.createdAt.goe(startDate.atStartOfDay())
                : null;
    }
    private BooleanExpression endDateLt(LocalDate endDate) {
        return endDate != null
                ? order.createdAt.lt(endDate.atStartOfDay().plusDays(1))
                : null;
    }
}