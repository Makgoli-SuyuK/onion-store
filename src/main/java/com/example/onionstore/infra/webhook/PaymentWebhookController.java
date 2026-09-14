package com.example.onionstore.infra.webhook;

import com.example.onionstore.global.dto.ApiResponse;
import com.example.onionstore.global.exception.ErrorCode;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PortOne 웹훅을 수신하고 검증 후 처리 로직에 위임하는 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PortOneWebhookVerifier webhookVerifier;
    private final PaymentWebhookHandler webhookHandler;

    @PostMapping("/api/webhooks/portone")
    public ResponseEntity<ApiResponse<Void>> receive(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String timestamp,
            @RequestHeader("webhook-signature") String signature,
            @RequestBody String body
    ) {
        Webhook webhook;
        try {
            webhook = webhookVerifier.verify(body, webhookId, signature, timestamp);
        } catch (WebhookVerificationException e) {
            log.warn("[Webhook] verification failed. webhookId={}, reason={}", webhookId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.WEBHOOK_VERIFICATION_FAILED));
        }
        webhookHandler.handle(webhookId, webhook, body);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


}
