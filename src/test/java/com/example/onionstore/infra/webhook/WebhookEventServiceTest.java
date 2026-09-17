package com.example.onionstore.infra.webhook;

import com.example.onionstore.global.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebhookEventServiceTest {
    WebhookEventRepository repository = mock(WebhookEventRepository.class);
    WebhookEventService service = new WebhookEventService(repository);
    WebhookEvent event = new WebhookEvent("id", "type", "pay", "{}");

    @Test
    void 중복이벤트는_저장하지_않는다() {
        // given
        when(repository.findByWebhookId("id")).thenReturn(Optional.of(event));

        // when
        var result = service.registerOrGet("id", "type", "pay", "{}");

        // then
        assertSame(event, result);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void 신규이벤트를_저장한다() {
        // given
        when(repository.findByWebhookId("id")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        // when
        var saved = service.registerOrGet("id", "type", "pay", "{}");

        // then
        assertEquals("id", saved.getWebhookId());
        assertEquals("{}", saved.getPayload());
        assertEquals(WebhookProcessingStatus.RECEIVED, saved.getProcessingStatus());
    }

    @Test
    void 중복저장충돌이면_기존이벤트를_반환한다() {
        // given
        when(repository.findByWebhookId("id")).thenReturn(Optional.empty()).thenReturn(Optional.of(event));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        // when
        var result = service.registerOrGet("id", "type", "pay", "{}");

        // then
        assertSame(event, result);
    }

    @Test
    void 중복외의_저장오류는_전파한다() {
        // given
        when(repository.findByWebhookId("id")).thenReturn(Optional.empty());
        var error = new DataIntegrityViolationException("invalid");
        when(repository.saveAndFlush(any())).thenThrow(error);

        // when
        var exception = assertThrows(DataIntegrityViolationException.class,
                () -> service.registerOrGet("id", "type", "pay", "{}"));

        // then
        assertSame(error, exception);
    }

    @Test
    void 없는이벤트_상태변경은_실패한다() {
        // given
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        // when
        var exception = assertThrows(BusinessException.class,
                () -> service.markProcessed(1L));

        // then
        assertEquals(ErrorCode.WEBHOOK_EVENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 잠금조회한_이벤트의_처리를_선점한다() {
        // given
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        // when
        WebhookClaimResult result = service.claimProcessing(1L);

        // then
        assertEquals(WebhookClaimResult.CLAIMED, result);
        assertEquals(WebhookProcessingStatus.PROCESSING, event.getProcessingStatus());
        assertNotNull(event.getProcessingStartedAt());
    }

    @Test
    void 없는이벤트는_처리선점을_할수_없다() {
        // given
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        // when
        var exception = assertThrows(BusinessException.class,
                () -> service.claimProcessing(1L));

        // then
        assertEquals(ErrorCode.WEBHOOK_EVENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 잠금조회한_이벤트의_상태를_변경한다() {
        // given
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        // when
        boolean failed = service.markFailed(1L, "failure");
        boolean processed = service.markProcessed(1L);
        boolean ignored = service.markIgnored(1L, "late");

        // then
        assertTrue(failed);
        assertTrue(processed);
        assertFalse(ignored);
        assertEquals(WebhookProcessingStatus.PROCESSED, event.getProcessingStatus());
    }
}
