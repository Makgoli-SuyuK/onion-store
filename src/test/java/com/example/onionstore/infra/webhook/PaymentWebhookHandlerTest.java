package com.example.onionstore.infra.webhook;

import com.example.onionstore.domain.payment.facade.PaymentFacade;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import io.portone.sdk.server.webhook.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentWebhookHandlerTest {
    PaymentFacade facade = mock(PaymentFacade.class);
    WebhookEventService events = mock(WebhookEventService.class);
    PaymentWebhookHandler handler = new PaymentWebhookHandler(facade, events);
    WebhookTransactionPaid paid = mock(WebhookTransactionPaid.class, RETURNS_DEEP_STUBS);
    WebhookEvent event = new WebhookEvent("id", "paid", "pay", "{}");

    void prepare(Webhook webhook) {
        ReflectionTestUtils.setField(event, "id", 1L);
        when(events.registerOrGet(eq("id"), anyString(), nullable(String.class), eq("{}"))).thenReturn(event);
        when(events.claimProcessing(1L)).thenReturn(WebhookClaimResult.CLAIMED);
    }

    @Test
    void 성공웹훅은_결제확정후_처리완료한다() {
        // given
        when(paid.getData().getPaymentId()).thenReturn("pay");
        prepare(paid);

        // when
        handler.handle("id", paid, "{}");

        // then
        var order = inOrder(facade, events);
        order.verify(events).claimProcessing(1L);
        order.verify(facade).confirmPaymentFromWebhook("pay");
        order.verify(events).markProcessed(1L);
    }

    @Test
    void 완료된_웹훅은_중복확정하지_않는다() {
        // given
        when(paid.getData().getPaymentId()).thenReturn("pay");
        prepare(paid);
        when(events.claimProcessing(1L)).thenReturn(WebhookClaimResult.ALREADY_COMPLETED);

        // when
        handler.handle("id", paid, "{}");

        // then
        verifyNoInteractions(facade);
        verify(events, never()).markProcessed(anyLong());
        verify(events, never()).markFailed(anyLong(), anyString());
    }

    @Test
    void 처리중인_웹훅은_중복처리하지_않고_충돌오류를_반환한다() {
        // given
        when(paid.getData().getPaymentId()).thenReturn("pay");
        prepare(paid);
        when(events.claimProcessing(1L)).thenReturn(WebhookClaimResult.ALREADY_PROCESSING);

        // when
        var exception = assertThrows(BusinessException.class,
                () -> handler.handle("id", paid, "{}"));

        // then
        assertEquals(ErrorCode.WEBHOOK_ALREADY_PROCESSING, exception.getErrorCode());
        verifyNoInteractions(facade);
        verify(events, never()).markFailed(anyLong(), anyString());
        verify(events, never()).markProcessed(anyLong());
    }

    @Test
    void 실패했던_웹훅은_재시도한다() {
        // given
        when(paid.getData().getPaymentId()).thenReturn("pay");
        prepare(paid);
        event.markFailed("previous failure");

        // when
        handler.handle("id", paid, "{}");

        // then
        verify(facade).confirmPaymentFromWebhook("pay");
        verify(events).markProcessed(1L);
    }

    @Test
    void 처리예외는_기록하고_전파한다() {
        // given
        when(paid.getData().getPaymentId()).thenReturn("pay");
        prepare(paid);
        var error = new IllegalStateException("gateway down");
        doThrow(error).when(facade).confirmPaymentFromWebhook("pay");

        // when
        var exception = assertThrows(IllegalStateException.class,
                () -> handler.handle("id", paid, "{}"));

        // then
        assertSame(error, exception);
        verify(events).markFailed(1L, "gateway down");
        verify(events, never()).markProcessed(anyLong());
    }

    @Test
    void 실패웹훅의_실제상태가_실패면_처리완료한다() {
        // given
        var failed = mock(WebhookTransactionFailed.class, RETURNS_DEEP_STUBS);
        when(failed.getData().getPaymentId()).thenReturn("pay");
        prepare(failed);
        when(facade.synchronizePaymentFailureFromWebhook("pay")).thenReturn(true);

        // when
        handler.handle("id", failed, "{}");

        // then
        verify(events).markProcessed(1L);
    }

    @Test
    void 실패웹훅의_실제상태가_실패가_아니면_무시한다() {
        // given
        var failed = mock(WebhookTransactionFailed.class, RETURNS_DEEP_STUBS);
        when(failed.getData().getPaymentId()).thenReturn("pay");
        prepare(failed);

        // when
        handler.handle("id", failed, "{}");

        // then
        verify(events).markIgnored(1L, "PortOne 실제 상태가 FAILED가 아님");
        verify(events, never()).markProcessed(anyLong());
    }

    @Test
    void 지원하지_않는_이벤트는_무시한다() {
        // given
        var unsupported = mock(WebhookTransactionCancelled.class);
        prepare(unsupported);

        // when
        handler.handle("id", unsupported, "{}");

        // then
        verifyNoInteractions(facade);
        verify(events).markIgnored(1L, "처리대상 아님");
    }
}
