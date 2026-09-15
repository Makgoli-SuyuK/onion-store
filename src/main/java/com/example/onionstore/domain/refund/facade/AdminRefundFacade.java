package com.example.onionstore.domain.refund.facade;

import com.example.onionstore.domain.refund.dto.admin.request.AdminRefundSearchCondition;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundDetailResponse;
import com.example.onionstore.domain.refund.dto.admin.response.AdminRefundPageResponse;
import com.example.onionstore.domain.refund.dto.common.RefundItemDetailResponse;
import com.example.onionstore.domain.refund.repository.dto.AdminRefundDetailProjection;
import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;
import com.example.onionstore.domain.refund.service.RefundService;
import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.service.UserService;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminRefundFacade {

    private final UserService userService;
    private final RefundService refundService;

    // 관리자 권한을 확인한 뒤 검색 조건에 맞는 환불 목록을 반환한다.
    @Transactional(readOnly = true)
    public AdminRefundPageResponse getRefunds(
            Long adminId,
            AdminRefundSearchCondition condition,
            int page,
            int size
    ) {
        validateAdmin(adminId);
        return AdminRefundPageResponse.from(refundService.searchAdminRefunds(
                condition,
                PageRequest.of(page - 1, size)
        ));
    }

    // 관리자만 주문·고객 정보를 포함한 환불 상세를 조회할 수 있다.
    @Transactional(readOnly = true)
    public AdminRefundDetailResponse getRefundDetail(Long adminId, Long refundId) {
        validateAdmin(adminId);
        AdminRefundDetailProjection projection = refundService
                .getAdminRefundDetailProjection(refundId);
        List<OrderItemSummaryRow> orderItems = refundService
                .getOrderItemSummaryRows(projection.orderId());
        List<RefundItemDetailResponse> refundItems = refundService
                .getRefundItemDetails(refundId);

        return AdminRefundDetailResponse.of(projection, orderItems, refundItems);
    }

    // 환불 검토 화면은 관리자 역할에만 허용한다.
    private void validateAdmin(Long userId) {
        if (userService.findUser(userId).getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ROLE);
        }
    }
}