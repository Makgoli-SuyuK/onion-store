package com.example.onionstore.infra.webhook;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentWebhookControllerTest {
    PortOneWebhookVerifier verifier = mock(PortOneWebhookVerifier.class);
    PaymentWebhookHandler handler = mock(PaymentWebhookHandler.class);
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new PaymentWebhookController(verifier, handler)).build();

    @Test
    void 검증성공이면_원본문을_핸들러로_전달한다() throws Exception {
        // given
        var webhook = mock(WebhookTransactionPaid.class);
        when(verifier.verify("{ }", "id", "signature", "123")).thenReturn(webhook);

        var request = post("/api/webhooks/portone").header("webhook-id", "id")
                .header("webhook-signature", "signature").header("webhook-timestamp", "123")
                .contentType("application/json").content("{ }");

        // when
        var result = mvc.perform(request);

        // then
        result.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        verify(handler).handle("id", webhook, "{ }");
    }

    @Test
    void 검증실패면_400이며_핸들러를_호출하지_않는다() throws Exception {
        // given
        when(verifier.verify(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(mock(WebhookVerificationException.class));

        var request = post("/api/webhooks/portone").header("webhook-id", "id")
                .header("webhook-signature", "invalid").header("webhook-timestamp", "123")
                .contentType("application/json").content("{}");

        // when
        var result = mvc.perform(request);

        // then
        result.andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(handler);
    }

    @Test
    void 필수헤더누락은_400이다() throws Exception {
        // given
        var request = post("/api/webhooks/portone").contentType("application/json").content("{}");

        // when
        var result = mvc.perform(request);

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(verifier, handler);
    }
}
