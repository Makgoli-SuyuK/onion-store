package com.example.onionstore.domain.payment.port;

public enum GatewayCancellationStatus {
    REQUESTED,
    SUCCEEDED,
    FAILED,
    UNKNOWN;

    public static GatewayCancellationStatus from(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return GatewayCancellationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return UNKNOWN;
        }
    }
}
