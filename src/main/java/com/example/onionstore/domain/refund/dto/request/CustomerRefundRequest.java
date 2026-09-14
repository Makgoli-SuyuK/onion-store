package com.example.onionstore.domain.refund.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CustomerRefundRequest(
        @NotBlank @Size(max = 500) String reason,
        @NotEmpty @Valid List< RefundItemRequest> items
) {
}