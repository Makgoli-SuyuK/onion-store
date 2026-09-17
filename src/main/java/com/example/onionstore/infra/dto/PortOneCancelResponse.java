package com.example.onionstore.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneCancelResponse(PortOneCancellation cancellation) {
}
