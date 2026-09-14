package com.example.onionstore.domain.refund.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

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
