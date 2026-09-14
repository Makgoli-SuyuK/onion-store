package com.example.onionstore.infra.webhook;

public enum WebhookProcessingStatus {
    RECEIVED,
    PROCESSING,
    PROCESSED,
    IGNORED,
    FAILED
}
