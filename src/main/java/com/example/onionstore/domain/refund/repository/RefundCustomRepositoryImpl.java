package com.example.onionstore.domain.refund.repository;

import com.example.onionstore.domain.refund.dto.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.response.AdminRefundListResponse;
import com.example.onionstore.domain.refund.dto.response.CustomerRefundSummaryResponse;
import com.example.onionstore.domain.refund.dto.response.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailHeader;
import com.example.onionstore.domain.refund.repository.dto.CustomerRefundDetailHeader;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import com.example.onionstore.domain.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.example.onionstore.domain.order.entity.QOrder.order;
import static com.example.onionstore.domain.order.entity.QOrderItem.orderItem;
import static com.example.onionstore.domain.payment.entity.QPayment.payment;
import static com.example.onionstore.domain.refund.entity.QRefund.refund;
import static com.example.onionstore.domain.refund.entity.QRefundItem.refundItem;
import static com.example.onionstore.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class RefundCustomRepositoryImpl implements RefundCustomRepository {

    private final JPAQueryFactory queryFactory;

    // 환불 검토 관리자
    private static final QUser reviewer = new QUser("reviewer");


    // =========================
    // 고객 환불 조회
    // =========================

    // 고객이 요청한 환불 목록 최신순
    @Override
    public List<CustomerRefundSummaryResponse> findCustomerRefunds(Long userId) {
        return queryFactory
                .select(Projections.constructor(
                        CustomerRefundSummaryResponse.class,
                        refund.id,
                        refund.status,
                        refund.amount,
                        refund.createdAt,
                        refund.reviewedAt,
                        refund.rejectionReason
                ))
                .from(refund)
                .join(refund.payment, payment)
                .join(payment.order, order)
                .where(order.user.id.eq(userId))
                .orderBy(refund.createdAt.desc(), refund.id.desc())
                .fetch();
    }

    // 환불ID와 사용자ID를 조건으로 다른사용자의 환불 상세조회 방지
    @Override
    public Optional<CustomerRefundDetailHeader> findCustomerRefundHeader(Long refundId, Long userId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(
                        CustomerRefundDetailHeader.class,
                        order.id,
                        order.orderNumber,
                        order.totalPrice,
                        refund.id,
                        refund.status,
                        refund.initiator,
                        refund.reasonType,
                        refund.reason,
                        refund.amount,
                        refund.createdAt,
                        refund.reviewedAt,
                        refund.rejectionReason
                ))
                .from(refund)
                .join(refund.payment, payment)
                .join(payment.order, order)
                .where(refund.id.eq(refundId),
                        order.user.id.eq(userId)
                )
                .fetchOne());

    }

    // =========================
    // 환불 상품 조회
    // =========================

    // 주문 전체 상품의 이름과 주문 수량 조회
    @Override
    public List<OrderItemSummaryRow> findOrderItemSummaryRows(Long orderId) {
        return queryFactory
                .select(Projections.constructor(
                        OrderItemSummaryRow.class,
                        orderItem.productName,
                        orderItem.quantity
                ))
                .from(orderItem)
                .join(orderItem.order, order)
                .where(order.id.eq(orderId))
                .orderBy(orderItem.id.asc())
                .fetch();

    }

    // 특정환불에 포함된 상품과 환불 수량 조회
    @Override
    public List<RefundItemDetailResponse> findRefundItemDetails(Long refundId) {
        return queryFactory
                .select(Projections.constructor(
                        RefundItemDetailResponse.class,
                        orderItem.id,
                        orderItem.productName,
                        orderItem.quantity,
                        refundItem.quantity
                ))
                .from(refundItem)
                .join(refundItem.orderItem, orderItem)
                .where(refundItem.refund.id.eq(refundId))
                .orderBy(refundItem.id.asc())
                .fetch();
    }


    // =========================
    // 관리자 환불 조회
    // =========================

    // 상태, 요청기간, 주문번호, 고객명으로 관리자용 환불 목록조회
    @Override
    public Page<AdminRefundListResponse> searchAdminRefunds(AdminRefundSearchCondition condition, Pageable pageable) {
        BooleanBuilder conditions = allConditions(condition);

        List<AdminRefundListResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminRefundListResponse.class,
                        refund.id,
                        order.orderNumber,
                        user.name,
                        refund.createdAt,
                        refund.status,
                        order.totalPrice,
                        refund.amount,
                        refund.initiator
                ))
                .from(refund)
                .join(refund.payment, payment)
                .join(payment.order, order)
                .join(order.user, user)
                .where(conditions)
                .orderBy(refund.createdAt.desc(), refund.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = queryFactory
                .select(refund.count())
                .from(refund)
                .join(refund.payment, payment)
                .join(payment.order, order)
                .join(order.user, user)
                .where(conditions)
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // 관리자 환불 상세 화면에 필요한 주문,고객,환불,검토 관리자 조회
    @Override
    public Optional<AdminRefundDetailHeader> findAdminRefundDetailHeader(Long refundId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(
                        AdminRefundDetailHeader.class,
                        order.id,
                        order.orderNumber,
                        order.totalPrice,
                        refund.id,
                        refund.status,
                        refund.initiator,
                        refund.reasonType,
                        refund.reason,
                        refund.amount,
                        refund.createdAt,
                        refund.reviewedAt,
                        refund.rejectionReason,
                        user.name,
                        user.phoneNumber,
                        user.email,
                        reviewer.name,
                        refund.portoneCancellationId
                ))
                .from(refund)
                .join(refund.payment, payment)
                .join(payment.order, order)
                .join(order.user, user)
                .leftJoin(refund.reviewedBy, reviewer)
                .where(refund.id.eq(refundId))
                .fetchOne()
        );
    }

    // =========================
    // 관리자 검색 조건
    // =========================

    /**
     * 요청 기간은 (from, to) 범위로 조회한다.
     * keyword는 주문번호 또는 고객명에 적용한다.
     */
    private BooleanBuilder allConditions(AdminRefundSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.status() != null) {
            builder.and(refund.status.eq(condition.status()));
        }
        if (condition.from() != null) {
            builder.and(refund.createdAt.goe(condition.from().atStartOfDay()));
        }
        if (condition.to() != null) {
            builder.and(refund.createdAt.lt(condition.to().plusDays(1).atStartOfDay()));
        }
        if (StringUtils.hasText(condition.keyword())) {
            builder.and(order.orderNumber.contains(condition.keyword()).or(user.name.contains(condition.keyword())));
        }
        return builder;
    }

}
