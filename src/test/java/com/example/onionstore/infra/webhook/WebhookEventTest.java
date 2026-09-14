package com.example.onionstore.infra.webhook;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebhookEventTest {

    @Test
    void 수신이벤트를_선점하면_처리중상태와_시작시각을_기록한다() {
        // given
        var event = new WebhookEvent("id", "type", "pay", "{}");

        // when
        WebhookClaimResult result = event.claimProcessing();

        // then
        assertEquals(WebhookClaimResult.CLAIMED, result);
        assertEquals(WebhookProcessingStatus.PROCESSING, event.getProcessingStatus());
        assertNotNull(event.getProcessingStartedAt());
        assertNull(event.getProcessingMessage());
        assertNull(event.getCompletedAt());
    }

    @Test
    void 처리중인_이벤트는_다시_선점할수_없다() {
        // given
        var event = new WebhookEvent("id", "type", "pay", "{}");
        event.claimProcessing();
        var processingStartedAt = event.getProcessingStartedAt();

        // when
        WebhookClaimResult result = event.claimProcessing();

        // then
        assertEquals(WebhookClaimResult.ALREADY_PROCESSING, result);
        assertEquals(WebhookProcessingStatus.PROCESSING, event.getProcessingStatus());
        assertEquals(processingStartedAt, event.getProcessingStartedAt());
    }

    @Test
    void 완료된_이벤트는_다시_선점할수_없다() {
        // given
        var event = new WebhookEvent("id", "type", "pay", "{}");
        event.claimProcessing();
        event.markProcessed();

        // when
        WebhookClaimResult result = event.claimProcessing();

        // then
        assertEquals(WebhookClaimResult.ALREADY_COMPLETED, result);
        assertEquals(WebhookProcessingStatus.PROCESSED, event.getProcessingStatus());
    }

    @Test
    void 실패이벤트는_재선점할때_이전실패정보를_초기화한다() {
        // given
        var event = new WebhookEvent("id", "type", "pay", "{}");
        event.claimProcessing();
        event.markFailed("gateway down");

        // when
        WebhookClaimResult result = event.claimProcessing();

        // then
        assertEquals(WebhookClaimResult.CLAIMED, result);
        assertEquals(WebhookProcessingStatus.PROCESSING, event.getProcessingStatus());
        assertNull(event.getProcessingMessage());
        assertNull(event.getCompletedAt());
        assertNotNull(event.getProcessingStartedAt());
    }

    @Test
    void 실패이벤트는_재처리할수_있다() {
        // given
        var event = new WebhookEvent("id", "type", "pay", "{}");
        var initialStatus = event.getProcessingStatus();
        boolean failed = event.markFailed("failure");

        // when
        boolean processed = event.markProcessed();
        var completedAt = event.getCompletedAt();
        boolean lateFailed = event.markFailed("late failure");
        boolean lateIgnored = event.markIgnored("late ignore");
        boolean duplicateProcessed = event.markProcessed();

        // then
        assertEquals(WebhookProcessingStatus.RECEIVED, initialStatus);
        assertTrue(failed);
        assertTrue(processed);
        assertFalse(lateFailed);
        assertFalse(lateIgnored);
        assertFalse(duplicateProcessed);
        assertEquals(WebhookProcessingStatus.PROCESSED, event.getProcessingStatus());
        assertNull(event.getProcessingMessage());
        assertNotNull(completedAt);
        assertEquals(completedAt, event.getCompletedAt());
    }

    @Test
    void 무시한이벤트는_다시처리하지_않는다() {
        // given
        var event = new WebhookEvent("id", "type", null, "{}");
        String message = "x".repeat(501);

        // when
        boolean ignored = event.markIgnored(message);
        boolean processed = event.markProcessed();

        // then
        assertTrue(ignored);
        assertFalse(processed);
        assertEquals(500, event.getProcessingMessage().length());
        assertEquals(WebhookProcessingStatus.IGNORED, event.getProcessingStatus());
    }
}
