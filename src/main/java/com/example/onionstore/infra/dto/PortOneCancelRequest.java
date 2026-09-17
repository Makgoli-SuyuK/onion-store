package com.example.onionstore.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortOneCancelRequest(
        String reason,
        String storeId,
        Long amount
) {
    public PortOneCancelRequest(String reason, String storeId) {
        this(reason, storeId, null);
    }
}
