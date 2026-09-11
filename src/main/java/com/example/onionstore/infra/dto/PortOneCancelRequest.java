package com.example.onionstore.infra.dto;

public record PortOneCancelRequest(
        String reason,
        String storeId
) {
}
