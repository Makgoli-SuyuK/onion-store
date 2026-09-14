package com.example.onionstore.domain.refund.dto.request;

import com.example.onionstore.domain.refund.entity.RefundStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// 관리자가 환불 목록을 검색할 때 사용하는 검색 조건
public record AdminRefundSearchCondition(
        RefundStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate to,
        String keyword
) {
}
