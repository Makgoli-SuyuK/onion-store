package com.example.onionstore.domain.refund.dto.admin.response;

import org.springframework.data.domain.Page;

import java.util.List;

// 관리자 환불 목록 페이징 응답
public record AdminRefundPageResponse(
        List<AdminRefundListResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AdminRefundPageResponse from(Page<AdminRefundListResponse> result) {
        return new AdminRefundPageResponse(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
