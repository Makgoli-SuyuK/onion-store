package com.example.onionstore.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(
        String id,
        String status,
        Amount amount
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(Long total) {

    }


}
