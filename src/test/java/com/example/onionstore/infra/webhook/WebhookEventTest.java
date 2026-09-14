package com.example.onionstore.infra.webhook;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebhookEventTest {

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
