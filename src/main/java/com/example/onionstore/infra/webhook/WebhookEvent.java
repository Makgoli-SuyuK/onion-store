package com.example.onionstore.infra.webhook;

import com.example.onionstore.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "webhook_events")
public class WebhookEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_id", nullable = false, unique = true, length = 200)
    private String webhookId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "portone_payment_id", length = 255)
    private String portonePaymentId;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private WebhookProcessingStatus processingStatus;

    @Column(name = "processing_message", length = 500)
    private String processingMessage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public WebhookEvent(String webhookId, String eventType, String portonePaymentId, String rawPayload) {
        this.webhookId = webhookId;
        this.eventType = eventType;
        this.portonePaymentId = portonePaymentId;
        this.rawPayload = rawPayload;
        this.processingStatus = WebhookProcessingStatus.RECEIVED;
    }

    public boolean markProcessed() {
        if (isTerminal()) {
            return false;
        }
        this.processingStatus = WebhookProcessingStatus.PROCESSED;
        this.processingMessage = null;
        this.completedAt = LocalDateTime.now();
        return true;
    }

    public boolean markIgnored(String message) {
        if (isTerminal()) {
            return false;
        }
        this.processingStatus = WebhookProcessingStatus.IGNORED;
        this.processingMessage = truncate(message);
        this.completedAt = LocalDateTime.now();
        return true;
    }
    public boolean markFailed(String message) {
        if (isTerminal()) {
            return false;
        }
        this.processingStatus = WebhookProcessingStatus.FAILED;
        this.processingMessage = truncate(message);
        this.completedAt = LocalDateTime.now();
        return true;
    }

    private boolean isTerminal() {
        return processingStatus == WebhookProcessingStatus.PROCESSED
                || processingStatus == WebhookProcessingStatus.IGNORED;
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 500) {
            return message;
        }
        return message.substring(0, 500);
    }

}
