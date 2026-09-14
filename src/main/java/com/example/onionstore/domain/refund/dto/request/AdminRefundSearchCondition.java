package com.example.onionstore.domain.refund.dto.request;

import com.example.onionstore.domain.refund.entity.RefundStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AdminRefundSearchCondition(
        RefundStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        String keyword
) {
}
