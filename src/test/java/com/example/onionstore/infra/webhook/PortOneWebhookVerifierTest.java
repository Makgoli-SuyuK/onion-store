package com.example.onionstore.infra.webhook;

import com.example.onionstore.infra.config.PortOneProperties;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import org.junit.jupiter.api.Test;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class PortOneWebhookVerifierTest {
    static final String BODY = "{\"type\":\"Transaction.Paid\",\"timestamp\":\"2026-09-14T00:00:00Z\",\"data\":{\"paymentId\":\"pay\",\"storeId\":\"store\",\"transactionId\":\"tx\"}}";
    static final byte[] KEY = "test-only-webhook-secret-12345678".getBytes(StandardCharsets.UTF_8);
    PortOneWebhookVerifier verifier() {
        var properties = new PortOneProperties();
        properties.setWebhookSecret("whsec_" + Base64.getEncoder().encodeToString(KEY));
        return new PortOneWebhookVerifier(properties);
    }
    String signature(String body, String timestamp) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(KEY, "HmacSHA256"));
        return "v1," + Base64.getEncoder().encodeToString(mac.doFinal(("id." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void 올바른서명은_결제웹훅으로_변환한다() throws Exception {
        // given
        var verifier = verifier();
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String signed = signature(BODY, timestamp);

        // when
        var result = verifier.verify(BODY, "id", signed, timestamp);

        // then
        var webhook = assertInstanceOf(WebhookTransactionPaid.class, result);
        assertEquals("pay", webhook.getData().getPaymentId());
    }

    @Test
    void 변조된본문은_거절한다() throws Exception {
        // given
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String signed = signature(BODY, timestamp);

        // when
        var exception = assertThrows(WebhookVerificationException.class,
                () -> verifier().verify(BODY.replace("pay", "other"), "id", signed, timestamp));

        // then
        assertNotNull(exception);
    }

    @Test
    void 오래된요청은_올바른서명이어도_거절한다() throws Exception {
        // given
        String timestamp = Long.toString(Instant.now().minusSeconds(600).getEpochSecond());
        String signed = signature(BODY, timestamp);

        // when
        var exception = assertThrows(WebhookVerificationException.class,
                () -> verifier().verify(BODY, "id", signed, timestamp));

        // then
        assertNotNull(exception);
    }

    @Test
    void 필수헤더가_없으면_거절한다() {
        // given
        var verifier = verifier();

        // when
        var exception = assertThrows(WebhookVerificationException.class,
                () -> verifier.verify(BODY, null, null, null));

        // then
        assertNotNull(exception);
    }
}
